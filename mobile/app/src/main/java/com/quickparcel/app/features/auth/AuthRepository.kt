package com.quickparcel.app.features.auth

import com.google.gson.Gson
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val retrofitClient: RetrofitClient,
    private val tokenManager: TokenManager
) {
    private val api = retrofitClient.create(AuthApiService::class.java)

    suspend fun login(email: String, password: String): AuthModels.AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(AuthModels.LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!
                    tokenManager.saveToken(userData.token)
                    tokenManager.saveUserData(Gson().toJson(userData))
                    AuthModels.AuthResult.Success(userData)
                } else {
                    AuthModels.AuthResult.Error(response.message() ?: "Login failed")
                }
            } catch (e: Exception) {
                AuthModels.AuthResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun register(request: AuthModels.RegisterRequest): AuthModels.AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.register(request)
                if (response.isSuccessful) {
                    AuthModels.AuthResult.Success(null)
                } else {
                    AuthModels.AuthResult.Error(response.message() ?: "Registration failed")
                }
            } catch (e: Exception) {
                AuthModels.AuthResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun logout() {
        tokenManager.clear()
    }
}