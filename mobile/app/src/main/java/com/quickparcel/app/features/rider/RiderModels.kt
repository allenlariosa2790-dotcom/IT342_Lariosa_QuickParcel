package com.quickparcel.app.features.rider

import com.quickparcel.app.shared.models.Delivery

sealed class RiderModels {

    data class AvailableDeliveriesResponse(
        val success: Boolean,
        val data: List<Delivery>
    )

    data class ActionResponse(
        val success: Boolean,
        val data: Delivery,
        val message: String? = null
    )

    data class ActiveDeliveryResponse(
        val success: Boolean,
        val data: Delivery?
    )

    data class DeliveriesResponse(
        val success: Boolean,
        val data: List<Delivery>
    )

    data class StatusUpdateRequest(
        val status: String,
        val location: String
    )

    data class EarningsStats(
        val today: Double,
        val thisWeek: Double,
        val lastWeek: Double,
        val total: Double,
        val completedCount: Int
    )

    data class WeeklyEarning(
        val day: String,
        val earnings: Double,
        val deliveries: Int
    )

    sealed class AvailableResult {
        data class Success(val deliveries: List<Delivery>) : AvailableResult()
        data class Error(val message: String) : AvailableResult()
    }

    sealed class AcceptResult {
        data class Success(val delivery: Delivery) : AcceptResult()
        data class Error(val message: String) : AcceptResult()
    }

    sealed class StatusResult {
        data class Success(val delivery: Delivery) : StatusResult()
        data class Error(val message: String) : StatusResult()
    }
}