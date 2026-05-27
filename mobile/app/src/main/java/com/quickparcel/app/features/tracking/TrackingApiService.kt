package com.quickparcel.app.features.tracking

import retrofit2.Response
import retrofit2.http.*

interface TrackingApiService {

    @GET("api/tracking/my")
    suspend fun getMyDeliveries(): Response<TrackingModels.TrackingResponse>

    @GET("api/tracking/delivery/{id}")
    suspend fun getDeliveryById(@Path("id") id: Int): Response<TrackingModels.DeliveryDetailResponse>

    @GET("api/tracking/delivery/{id}/history")
    suspend fun getTrackingHistory(@Path("id") id: Int): Response<TrackingModels.HistoryResponse>

    @GET("api/tracking/delivery/{id}/image")
    suspend fun getParcelImage(@Path("id") id: Int): Response<TrackingModels.ImageResponse>
}