package com.quickparcel.app.features.rider

import retrofit2.Response
import retrofit2.http.*

interface RiderApiService {

    @GET("api/rider/deliveries/available")
    suspend fun getAvailableDeliveries(): Response<RiderModels.AvailableDeliveriesResponse>

    @PUT("api/rider/deliveries/{id}/accept")
    suspend fun acceptDelivery(@Path("id") id: Int): Response<RiderModels.ActionResponse>

    @PUT("api/rider/deliveries/{id}/status")
    suspend fun updateDeliveryStatus(
        @Path("id") id: Int,
        @Body request: RiderModels.StatusUpdateRequest
    ): Response<RiderModels.ActionResponse>

    @GET("api/rider/deliveries/active")
    suspend fun getActiveDeliveries(): Response<RiderModels.ActiveDeliveriesResponse>

    @GET("api/tracking/my")
    suspend fun getMyDeliveries(): Response<RiderModels.DeliveriesResponse>
}