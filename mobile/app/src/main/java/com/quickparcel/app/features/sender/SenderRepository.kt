package com.quickparcel.app.features.sender

import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SenderRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(SenderApiService::class.java)

    suspend fun getDashboardData(): SenderModels.DashboardResult {
        return withContext(Dispatchers.IO) {
            try {
                println("=== SENDER DASHBOARD DEBUG ===")
                val response = api.getMyDeliveries()
                println("Response successful: ${response.isSuccessful}")
                println("Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val dashboardResponse = response.body()!!
                    println("Success field: ${dashboardResponse.success}")
                    println("Data size: ${dashboardResponse.data.size}")

                    val deliveries = dashboardResponse.data

                    val activeStatuses = listOf("PENDING", "ACCEPTED", "PICKED_UP", "IN_TRANSIT")
                    val stats = SenderModels.DashboardStats(
                        totalDeliveries = deliveries.size,
                        activeDeliveries = deliveries.count { d -> d.status in activeStatuses },
                        completedDeliveries = deliveries.count { d -> d.status == "DELIVERED" },
                        totalSpent = deliveries.filter { d -> d.status == "DELIVERED" }
                            .sumOf { d -> d.estimatedCost }
                    )
                    println("Stats: total=${stats.totalDeliveries}, active=${stats.activeDeliveries}, completed=${stats.completedDeliveries}, spent=${stats.totalSpent}")

                    val recentDeliveries = deliveries.sortedByDescending { d -> d.createdAt }.take(5)
                    SenderModels.DashboardResult.Success(stats, recentDeliveries)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Error response: $errorBody")
                    SenderModels.DashboardResult.Error(response.message() ?: "Failed to load dashboard")
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                e.printStackTrace()
                SenderModels.DashboardResult.Error(e.message ?: "Network error")
            }
        }
    }
}