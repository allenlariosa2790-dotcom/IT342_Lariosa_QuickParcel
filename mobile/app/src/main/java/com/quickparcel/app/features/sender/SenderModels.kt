package com.quickparcel.app.features.sender

import com.quickparcel.app.shared.models.Delivery

sealed class SenderModels {

    data class DashboardStats(
        val totalDeliveries: Int,
        val activeDeliveries: Int,
        val completedDeliveries: Int,
        val totalSpent: Double
    )

    data class DashboardResponse(
        val success: Boolean,
        val data: List<Delivery>
    )

    sealed class DashboardResult {
        data class Success(val stats: DashboardStats, val recentDeliveries: List<Delivery>) : DashboardResult()
        data class Error(val message: String) : DashboardResult()
    }
}