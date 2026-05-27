package com.quickparcel.app.features.admin

import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.models.User

sealed class AdminModels {

    data class DashboardStats(
        val totalUsers: Int,
        val totalSenders: Int,
        val totalRiders: Int,
        val totalDeliveries: Int,
        val pendingDeliveries: Int,
        val completedDeliveries: Int,
        val totalEarnings: Double,
        val activeRiders: Int
    )

    data class StatsResponse(
        val success: Boolean,
        val data: DashboardStats
    )

    data class UsersResponse(
        val success: Boolean,
        val data: List<User>
    )

    data class DeliveriesResponse(
        val success: Boolean,
        val data: List<Delivery>
    )

    data class UpdateUserStatusRequest(
        val isActive: Boolean
    )

    data class MessageResponse(
        val success: Boolean,
        val message: String
    )

    sealed class StatsResult {
        data class Success(val stats: DashboardStats) : StatsResult()
        data class Error(val message: String) : StatsResult()
    }

    sealed class UsersResult {
        data class Success(val users: List<User>) : UsersResult()
        data class Error(val message: String) : UsersResult()
    }

    sealed class DeliveriesResult {
        data class Success(val deliveries: List<Delivery>) : DeliveriesResult()
        data class Error(val message: String) : DeliveriesResult()
    }

    sealed class UserStatusResult {
        data class Success(val message: String) : UserStatusResult()
        data class Error(val message: String) : UserStatusResult()
    }

    sealed class CancelDeliveryResult {
        data class Success(val message: String) : CancelDeliveryResult()
        data class Error(val message: String) : CancelDeliveryResult()
    }
}