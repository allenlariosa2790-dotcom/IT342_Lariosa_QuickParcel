package com.quickparcel.app.features.auth

sealed class AuthModels {

    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class RegisterRequest(
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val phone: String,
        val userType: String
    )

    data class JwtResponse(
        val token: String,
        val id: Int,
        val email: String,
        val firstName: String,
        val lastName: String,
        val userType: String,
        val phone: String? = null,  // Add this
        val createdAt: String? = null  // Add this
    )

    data class MessageResponse(
        val message: String
    )

    sealed class AuthResult {
        data class Success(val user: JwtResponse?) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}