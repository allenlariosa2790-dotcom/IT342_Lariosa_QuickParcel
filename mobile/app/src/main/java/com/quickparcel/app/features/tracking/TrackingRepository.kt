package com.quickparcel.app.features.tracking

import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.models.TrackingHistory
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackingRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(TrackingApiService::class.java)

    suspend fun getMyDeliveries(): TrackingModels.TrackingResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    val deliveries = response.body()!!.data
                    val activeStatuses = listOf("PENDING", "ACCEPTED", "PICKED_UP", "IN_TRANSIT")

                    val stats = TrackingModels.DeliveryStats(
                        total = deliveries.size,
                        active = deliveries.count { d -> d.status in activeStatuses },
                        completed = deliveries.count { d -> d.status == "DELIVERED" },
                        totalSpent = deliveries.filter { d -> d.status == "DELIVERED" }
                            .sumOf { d -> d.estimatedCost }
                    )

                    TrackingModels.TrackingResult.Success(deliveries, stats)
                } else {
                    TrackingModels.TrackingResult.Error(response.message() ?: "Failed to load deliveries")
                }
            } catch (e: Exception) {
                TrackingModels.TrackingResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getDeliveryById(id: Int): Delivery? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getDeliveryById(id)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.data
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getTrackingHistory(id: Int): List<TrackingHistory> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTrackingHistory(id)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.data
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getParcelImage(id: Int): TrackingModels.ImageResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getParcelImage(id)
                if (response.isSuccessful && response.body() != null) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}