package com.quickparcel.app.features.delivery

import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.models.ParcelRequest

sealed class DeliveryModels {

    data class CreateDeliveryRequest(
        val parcel: ParcelRequest,
        val pickupAddress: String,
        val dropoffAddress: String,
        val pickupLatitude: Double? = null,
        val pickupLongitude: Double? = null,
        val dropoffLatitude: Double? = null,
        val dropoffLongitude: Double? = null,
        val notes: String? = null,
        val scheduledTime: String? = null,
        val paymentMethod: String,
        val paymentStatus: String
    )  // <-- Fixed: Added closing parenthesis and brace

    data class CalculateDistanceRequest(
        val pickupAddress: String,
        val dropoffAddress: String,
        val weight: Double
    )

    data class DeliveryResponse(
        val success: Boolean,
        val data: Delivery
    )

    data class DistanceResponse(
        val distance: Double,
        val estimatedCost: Double
    )

    sealed class CreateDeliveryResult {
        data class Success(val delivery: Delivery) : CreateDeliveryResult()
        data class Error(val message: String) : CreateDeliveryResult()
    }

    sealed class DistanceResult {
        data class Success(val distance: Double, val estimatedCost: Double) : DistanceResult()
        data class Error(val message: String) : DistanceResult()
    }
}