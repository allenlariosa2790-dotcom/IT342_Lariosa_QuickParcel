package com.quickparcel.app.features.payment

import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(PaymentApiService::class.java)

    // Changed to get deliveries with payment info (like webapp)
    suspend fun getMyPayments(): PaymentModels.PaymentsResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getMyDeliveries()
                if (response.isSuccessful && response.body() != null) {
                    val deliveries = response.body()!!.data

                    val totalSpent = deliveries.filter { it.paymentStatus == "PAID" }
                        .sumOf { it.estimatedCost }
                    val totalTransactions = deliveries.size
                    val paidCount = deliveries.count { it.paymentStatus == "PAID" }
                    val pendingCount = deliveries.count { it.paymentStatus in listOf("PENDING", "UNPAID") || it.paymentStatus == null }

                    val stats = PaymentModels.PaymentStats(
                        totalSpent = totalSpent,
                        totalTransactions = totalTransactions,
                        pendingCount = pendingCount,
                        paidCount = paidCount
                    )

                    PaymentModels.PaymentsResult.Success(deliveries, stats)
                } else {
                    PaymentModels.PaymentsResult.Error(response.message() ?: "Failed to load payment history")
                }
            } catch (e: Exception) {
                PaymentModels.PaymentsResult.Error(e.message ?: "Network error")
            }
        }
    }

    // Keep existing methods for Stripe
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