package com.quickparcel.app.features.payment

import com.quickparcel.app.shared.models.Delivery

sealed class PaymentModels {

    // Use Delivery model with payment info (like webapp)
    data class DeliveryPaymentInfo(
        val id: Int,
        val trackingNumber: String,
        val estimatedCost: Double,
        val paymentMethod: String?,
        val paymentStatus: String?,
        val status: String,
        val createdAt: String,
        val pickupAddress: String,
        val dropoffAddress: String
    )

    data class DeliveriesResponse(
        val success: Boolean,
        val data: List<Delivery>
    )

    data class PaymentStats(
        val totalSpent: Double,
        val totalTransactions: Int,
        val pendingCount: Int,
        val paidCount: Int
    )

    sealed class PaymentsResult {
        data class Success(val deliveries: List<Delivery>, val stats: PaymentStats) : PaymentsResult()
        data class Error(val message: String) : PaymentsResult()
    }

    // Keep existing classes for Stripe payment
    data class CreatePaymentIntentRequest(
        val deliveryId: Int,
        val amount: Double,
        val description: String
    )

    data class PaymentIntentResponse(
        val clientSecret: String,
        val paymentIntentId: String
    )

    data class PaymentStatusResponse(
        val status: String
    )

    data class MarkPaidResponse(
        val success: Boolean,
        val message: String,
        val paymentStatus: String? = null
    )

    sealed class PaymentResult {
        data class Success(val clientSecret: String, val paymentIntentId: String) : PaymentResult()
        data class Error(val message: String) : PaymentResult()
    }

    sealed class StatusResult {
        data class Success(val status: String) : StatusResult()
        data class Error(val message: String) : StatusResult()
    }

    sealed class MarkPaidResult {
        data class Success(val message: String) : MarkPaidResult()
        data class Error(val message: String) : MarkPaidResult()
    }
}