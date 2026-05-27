package com.quickparcel.app.features.payment

import retrofit2.Response
import retrofit2.http.*

interface PaymentApiService {

    // Change to use tracking endpoint (like webapp)
    @GET("api/tracking/my")
    suspend fun getMyDeliveries(): Response<PaymentModels.DeliveriesResponse>

    @POST("api/payments/stripe/create-payment-intent")
    suspend fun createStripePaymentIntent(
        @Body request: PaymentModels.CreatePaymentIntentRequest
    ): Response<PaymentModels.PaymentIntentResponse>

    @GET("api/payments/stripe/status/{paymentIntentId}")
    suspend fun getStripePaymentStatus(
        @Path("paymentIntentId") paymentIntentId: String
    ): Response<PaymentModels.PaymentStatusResponse>

    @PUT("api/deliveries/{deliveryId}/mark-paid")
    suspend fun markDeliveryAsPaid(
        @Path("deliveryId") deliveryId: Int
    ): Response<PaymentModels.MarkPaidResponse>
}