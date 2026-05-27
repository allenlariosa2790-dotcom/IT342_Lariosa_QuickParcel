package com.quickparcel.app.features.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = ProfileRepository(retrofitClient)

    private val _updateResult = MutableSharedFlow<ProfileState>()
    val updateResult = _updateResult.asSharedFlow()

    private val _passwordResult = MutableSharedFlow<PasswordState>()
    val passwordResult = _passwordResult.asSharedFlow()

    private val _pictureResult = MutableSharedFlow<PictureState>()
    val pictureResult = _pictureResult.asSharedFlow()

    fun updateProfile(firstName: String, lastName: String, phone: String?) {
        viewModelScope.launch {
            _updateResult.emit(ProfileState.Loading)
            val result = repository.updateProfile(firstName, lastName, phone)
            when (result) {
                is ProfileModels.ProfileResult.Success -> {
                    _updateResult.emit(ProfileState.Success(result.message))
                }
                is ProfileModels.ProfileResult.Error -> {
                    _updateResult.emit(ProfileState.Error(result.message))
                }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _passwordResult.emit(PasswordState.Loading)
            val result = repository.changePassword(currentPassword, newPassword)
            when (result) {
                is ProfileModels.PasswordResult.Success -> {
                    _passwordResult.emit(PasswordState.Success(result.message))
                }
                is ProfileModels.PasswordResult.Error -> {
                    _passwordResult.emit(PasswordState.Error(result.message))
                }
            }
        }
    }

    fun loadProfilePicture() {
        viewModelScope.launch {
            _pictureResult.emit(PictureState.Loading)
            val result = repository.getProfilePicture()
            when (result) {
                is ProfileModels.PictureResult.Success -> {
                    _pictureResult.emit(PictureState.Success(result.url))
                }
                is ProfileModels.PictureResult.NoPicture -> {
                    _pictureResult.emit(PictureState.NoPicture)
                }
                is ProfileModels.PictureResult.Error -> {
                    _pictureResult.emit(PictureState.Error(result.message))
                }
            }
        }
    }

    fun uploadProfilePicture(imageUri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            _pictureResult.emit(PictureState.Loading)
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val file = File(context.cacheDir, "profile_temp.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val result = repository.uploadProfilePicture(part)
                when (result) {
                    is ProfileModels.PictureResult.Success -> {
                        _pictureResult.emit(PictureState.Success(result.url))
                    }
                    is ProfileModels.PictureResult.Error -> {
                        _pictureResult.emit(PictureState.Error(result.message))
                    }
                    else -> {}
                }
                file.delete()
            } catch (e: Exception) {
                _pictureResult.emit(PictureState.Error(e.message ?: "Failed to upload image"))
            }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            _pictureResult.emit(PictureState.Loading)
            val result = repository.deleteProfilePicture()
            when (result) {
                is ProfileModels.PictureResult.Success -> {
                    _pictureResult.emit(PictureState.Success(""))
                }
                is ProfileModels.PictureResult.Error -> {
                    _pictureResult.emit(PictureState.Error(result.message))
                }
                else -> {}
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class PasswordState {
    object Loading : PasswordState()
    data class Success(val message: String) : PasswordState()
    data class Error(val message: String) : PasswordState()
}

sealed class PictureState {
    object Loading : PictureState()
    data class Success(val url: String) : PictureState()
    object NoPicture : PictureState()
    data class Error(val message: String) : PictureState()
}