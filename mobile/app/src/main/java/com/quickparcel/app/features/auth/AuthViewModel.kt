package com.quickparcel.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.datastore.TokenManager
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val retrofitClient: RetrofitClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val authRepository = AuthRepository(retrofitClient, tokenManager)

    private val _loginResult = MutableSharedFlow<AuthResult>()
    val loginResult = _loginResult.asSharedFlow()

    private val _registerResult = MutableSharedFlow<RegisterResult>()
    val registerResult = _registerResult.asSharedFlow()

    fun login(email: String, password: String, selectedRole: String) {
        viewModelScope.launch {
            _loginResult.emit(AuthResult.Loading)
            val result = authRepository.login(email, password)
            when (result) {
                is AuthModels.AuthResult.Success -> {
                    val user = result.user
                    if (user != null) {
                        if (user.userType == selectedRole || user.userType == "ADMIN") {
                            _loginResult.emit(AuthResult.Success(user))
                        } else {
                            _loginResult.emit(AuthResult.Error("This account is registered as a ${user.userType}. Please use the ${user.userType} login tab."))
                        }
                    }
                }
                is AuthModels.AuthResult.Error -> {
                    _loginResult.emit(AuthResult.Error(result.message))
                }
            }
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String, phone: String, userType: String) {
        viewModelScope.launch {
            _registerResult.emit(RegisterResult.Loading)
            val request = AuthModels.RegisterRequest(email, password, firstName, lastName, phone, userType)
            val result = authRepository.register(request)
            when (result) {
                is AuthModels.AuthResult.Success -> {
                    _registerResult.emit(RegisterResult.Success)
                }
                is AuthModels.AuthResult.Error -> {
                    _registerResult.emit(RegisterResult.Error(result.message))
                }
            }
        }
    }
}

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: AuthModels.JwtResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class RegisterResult {
    object Loading : RegisterResult()
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}