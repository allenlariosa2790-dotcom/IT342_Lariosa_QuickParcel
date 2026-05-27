package com.quickparcel.app.features.sender

import retrofit2.Response
import retrofit2.http.GET

interface SenderApiService {

    @GET("api/tracking/my")  // Changed from "api/deliveries/my" to "api/tracking/my"
    suspend fun getMyDeliveries(): Response<SenderModels.DashboardResponse>
}