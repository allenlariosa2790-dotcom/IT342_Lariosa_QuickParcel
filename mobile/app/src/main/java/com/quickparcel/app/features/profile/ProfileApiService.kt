package com.quickparcel.app.features.profile

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Body request: ProfileModels.UpdateProfileRequest
    ): Response<ProfileModels.MessageResponse>

    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Body request: ProfileModels.ChangePasswordRequest
    ): Response<ProfileModels.MessageResponse>

    @GET("api/upload/profile-picture")
    suspend fun getProfilePicture(): Response<ProfileModels.ProfilePictureResponse>

    @POST("api/upload/profile-picture")
    @Multipart
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): Response<ProfileModels.UploadResponse>

    @DELETE("api/upload/profile-picture")
    suspend fun deleteProfilePicture(): Response<ProfileModels.MessageResponse>
}