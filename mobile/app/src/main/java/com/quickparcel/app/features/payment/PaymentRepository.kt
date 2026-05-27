package com.quickparcel.app.features.payment

import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(PaymentApiService::class.java)

    suspend fun createStripePaymentIntent(deliveryId: Int, amount: Double, description: String): PaymentModels.PaymentResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createStripePaymentIntent(
                    PaymentModels.CreatePaymentIntentRequest(deliveryId, amount, description)
                )
                if (response.isSuccessful && response.body() != null) {
                    PaymentModels.PaymentResult.Success(
                        response.body()!!.clientSecret,
                        response.body()!!.paymentIntentId
                    )
                } else {
                    PaymentModels.PaymentResult.Error(response.message() ?: "Failed to create payment intent")
                }
            } catch (e: Exception) {
                PaymentModels.PaymentResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getPaymentStatus(paymentIntentId: String): PaymentModels.StatusResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStripePaymentStatus(paymentIntentId)
                if (response.isSuccessful && response.body() != null) {
                    PaymentModels.StatusResult.Success(response.body()!!.status)
                } else {
                    PaymentModels.StatusResult.Error(response.message() ?: "Failed to get payment status")
                }
            } catch (e: Exception) {
                PaymentModels.StatusResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun markDeliveryAsPaid(deliveryId: Int): PaymentModels.MarkPaidResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.markDeliveryAsPaid(deliveryId)
                if (response.isSuccessful && response.body() != null) {
                    PaymentModels.MarkPaidResult.Success(response.body()!!.message)
                } else {
                    PaymentModels.MarkPaidResult.Error(response.message() ?: "Failed to mark as paid")
                }
            } catch (e: Exception) {
                PaymentModels.MarkPaidResult.Error(e.message ?: "Network error")
            }
        }
    }
}