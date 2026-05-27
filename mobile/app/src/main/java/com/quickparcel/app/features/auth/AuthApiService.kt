package com.quickparcel.app.features.auth

import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthModels.LoginRequest): Response<AuthModels.JwtResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthModels.RegisterRequest): Response<AuthModels.MessageResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<AuthModels.JwtResponse>
}