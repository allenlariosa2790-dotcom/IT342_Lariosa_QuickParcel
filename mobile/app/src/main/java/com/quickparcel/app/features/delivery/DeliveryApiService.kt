package com.quickparcel.app.features.delivery

import retrofit2.Response
import retrofit2.http.*

interface DeliveryApiService {

    @POST("api/deliveries")
    suspend fun createDelivery(
        @Body request: DeliveryModels.CreateDeliveryRequest
    ): Response<DeliveryModels.DeliveryResponse>

    @POST("api/deliveries/calculate-distance")
    suspend fun calculateDistance(
        @Body request: DeliveryModels.CalculateDistanceRequest
    ): Response<DeliveryModels.DistanceResponse>
}