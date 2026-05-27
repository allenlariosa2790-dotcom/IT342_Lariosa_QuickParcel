package com.quickparcel.app.features.profile

import com.quickparcel.app.shared.models.User

sealed class ProfileModels {

    data class UpdateProfileRequest(
        val firstName: String,
        val lastName: String,
        val phone: String?
    )

    data class ChangePasswordRequest(
        val currentPassword: String,
        val newPassword: String
    )

    data class MessageResponse(
        val success: Boolean,
        val message: String
    )

    data class ProfilePictureResponse(
        val hasPicture: Boolean,
        val url: String? = null
    )

    data class UploadResponse(
        val success: Boolean,
        val fileUrl: String,
        val message: String? = null
    )

    sealed class ProfileResult {
        data class Success(val message: String) : ProfileResult()
        data class Error(val message: String) : ProfileResult()
    }

    sealed class PasswordResult {
        data class Success(val message: String) : PasswordResult()
        data class Error(val message: String) : PasswordResult()
    }

    sealed class PictureResult {
        data class Success(val url: String) : PictureResult()
        data class Error(val message: String) : PictureResult()
        object NoPicture : PictureResult()
    }
}