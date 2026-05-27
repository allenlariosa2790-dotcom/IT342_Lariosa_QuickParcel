package com.quickparcel.app.features.payment

sealed class PaymentModels {

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