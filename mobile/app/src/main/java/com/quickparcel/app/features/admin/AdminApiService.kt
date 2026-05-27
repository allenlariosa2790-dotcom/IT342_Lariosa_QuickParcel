package com.quickparcel.app.features.admin

import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {

    @GET("api/admin/stats")
    suspend fun getDashboardStats(): Response<AdminModels.StatsResponse>

    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<AdminModels.UsersResponse>

    @PUT("api/admin/users/{userId}/status")
    suspend fun updateUserStatus(
        @Path("userId") userId: Int,
        @Body request: AdminModels.UpdateUserStatusRequest
    ): Response<AdminModels.MessageResponse>

    @GET("api/admin/deliveries")
    suspend fun getAllDeliveries(): Response<AdminModels.DeliveriesResponse>

    @PUT("api/admin/deliveries/{deliveryId}/cancel")
    suspend fun cancelDelivery(
        @Path("deliveryId") deliveryId: Int
    ): Response<AdminModels.MessageResponse>
}