package com.quickparcel.app.features.profile

import com.quickparcel.app.shared.network.RetrofitClient
import okhttp3.MultipartBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(ProfileApiService::class.java)

    suspend fun updateProfile(firstName: String, lastName: String, phone: String?): ProfileModels.ProfileResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateProfile(ProfileModels.UpdateProfileRequest(firstName, lastName, phone))
                if (response.isSuccessful && response.body() != null) {
                    ProfileModels.ProfileResult.Success(response.body()!!.message ?: "Profile updated successfully")
                } else {
                    ProfileModels.ProfileResult.Error(response.message() ?: "Failed to update profile")
                }
            } catch (e: Exception) {
                ProfileModels.ProfileResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): ProfileModels.PasswordResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.changePassword(ProfileModels.ChangePasswordRequest(currentPassword, newPassword))
                if (response.isSuccessful && response.body() != null) {
                    ProfileModels.PasswordResult.Success(response.body()!!.message)
                } else {
                    ProfileModels.PasswordResult.Error(response.message() ?: "Failed to change password")
                }
            } catch (e: Exception) {
                ProfileModels.PasswordResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun getProfilePicture(): ProfileModels.PictureResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getProfilePicture()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.hasPicture && data.url != null) {
                        ProfileModels.PictureResult.Success(data.url)
                    } else {
                        ProfileModels.PictureResult.NoPicture
                    }
                } else {
                    ProfileModels.PictureResult.Error(response.message() ?: "Failed to load profile picture")
                }
            } catch (e: Exception) {
                ProfileModels.PictureResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun uploadProfilePicture(filePart: MultipartBody.Part): ProfileModels.PictureResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.uploadProfilePicture(filePart)
                if (response.isSuccessful && response.body() != null) {
                    ProfileModels.PictureResult.Success(response.body()!!.fileUrl)
                } else {
                    ProfileModels.PictureResult.Error(response.message() ?: "Failed to upload picture")
                }
            } catch (e: Exception) {
                ProfileModels.PictureResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun deleteProfilePicture(): ProfileModels.PictureResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.deleteProfilePicture()
                if (response.isSuccessful && response.body() != null) {
                    ProfileModels.PictureResult.Success("")
                } else {
                    ProfileModels.PictureResult.Error(response.message() ?: "Failed to delete picture")
                }
            } catch (e: Exception) {
                ProfileModels.PictureResult.Error(e.message ?: "Network error")
            }
        }
    }
}