package com.quickparcel.app.features.tracking

import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.models.TrackingHistory

sealed class TrackingModels {

    data class TrackingResponse(
        val success: Boolean,
        val data: List<Delivery>
    )

    data class DeliveryDetailResponse(
        val success: Boolean,
        val data: Delivery
    )

    data class HistoryResponse(
        val success: Boolean,
        val data: List<TrackingHistory>
    )

    data class ImageResponse(
        val hasImage: Boolean,
        val imageUrl: String? = null
    )

    data class DeliveryStats(
        val total: Int,
        val active: Int,
        val completed: Int,
        val totalSpent: Double
    )

    sealed class TrackingResult {
        data class Success(val deliveries: List<Delivery>, val stats: DeliveryStats) : TrackingResult()
        data class Error(val message: String) : TrackingResult()
    }
}