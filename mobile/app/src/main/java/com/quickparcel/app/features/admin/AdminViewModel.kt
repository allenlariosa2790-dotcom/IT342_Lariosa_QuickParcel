package com.quickparcel.app.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.models.User
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = AdminRepository(retrofitClient)

    private val _statsResult = MutableSharedFlow<AdminStatsState>()
    val statsResult = _statsResult.asSharedFlow()

    private val _usersResult = MutableSharedFlow<AdminUsersState>()
    val usersResult = _usersResult.asSharedFlow()

    private val _deliveriesResult = MutableSharedFlow<AdminDeliveriesState>()
    val deliveriesResult = _deliveriesResult.asSharedFlow()

    private val _userStatusResult = MutableSharedFlow<AdminUserStatusState>()
    val userStatusResult = _userStatusResult.asSharedFlow()

    private val _cancelDeliveryResult = MutableSharedFlow<AdminCancelDeliveryState>()
    val cancelDeliveryResult = _cancelDeliveryResult.asSharedFlow()

    fun loadDashboardStats() {
        viewModelScope.launch {
            _statsResult.emit(AdminStatsState.Loading)
            val result = repository.getDashboardStats()
            when (result) {
                is AdminModels.StatsResult.Success -> {
                    _statsResult.emit(AdminStatsState.Success(result.stats))
                }
                is AdminModels.StatsResult.Error -> {
                    _statsResult.emit(AdminStatsState.Error(result.message))
                }
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _usersResult.emit(AdminUsersState.Loading)
            val result = repository.getAllUsers()
            when (result) {
                is AdminModels.UsersResult.Success -> {
                    _usersResult.emit(AdminUsersState.Success(result.users))
                }
                is AdminModels.UsersResult.Error -> {
                    _usersResult.emit(AdminUsersState.Error(result.message))
                }
            }
        }
    }

    fun loadAllDeliveries() {
        viewModelScope.launch {
            _deliveriesResult.emit(AdminDeliveriesState.Loading)
            val result = repository.getAllDeliveries()
            when (result) {
                is AdminModels.DeliveriesResult.Success -> {
                    _deliveriesResult.emit(AdminDeliveriesState.Success(result.deliveries))
                }
                is AdminModels.DeliveriesResult.Error -> {
                    _deliveriesResult.emit(AdminDeliveriesState.Error(result.message))
                }
            }
        }
    }

    fun updateUserStatus(userId: Int, isActive: Boolean) {
        viewModelScope.launch {
            _userStatusResult.emit(AdminUserStatusState.Loading)
            val result = repository.updateUserStatus(userId, isActive)
            when (result) {
                is AdminModels.UserStatusResult.Success -> {
                    _userStatusResult.emit(AdminUserStatusState.Success(result.message))
                    loadAllUsers() // Refresh user list
                }
                is AdminModels.UserStatusResult.Error -> {
                    _userStatusResult.emit(AdminUserStatusState.Error(result.message))
                }
            }
        }
    }

    fun cancelDelivery(deliveryId: Int) {
        viewModelScope.launch {
            _cancelDeliveryResult.emit(AdminCancelDeliveryState.Loading)
            val result = repository.cancelDelivery(deliveryId)
            when (result) {
                is AdminModels.CancelDeliveryResult.Success -> {
                    _cancelDeliveryResult.emit(AdminCancelDeliveryState.Success(result.message))
                    loadAllDeliveries() // Refresh delivery list
                    loadDashboardStats() // Refresh stats
                }
                is AdminModels.CancelDeliveryResult.Error -> {
                    _cancelDeliveryResult.emit(AdminCancelDeliveryState.Error(result.message))
                }
            }
        }
    }
}

sealed class AdminStatsState {
    object Loading : AdminStatsState()
    data class Success(val stats: AdminModels.DashboardStats) : AdminStatsState()
    data class Error(val message: String) : AdminStatsState()
}

sealed class AdminUsersState {
    object Loading : AdminUsersState()
    data class Success(val users: List<User>) : AdminUsersState()
    data class Error(val message: String) : AdminUsersState()
}

sealed class AdminDeliveriesState {
    object Loading : AdminDeliveriesState()
    data class Success(val deliveries: List<Delivery>) : AdminDeliveriesState()
    data class Error(val message: String) : AdminDeliveriesState()
}

sealed class AdminUserStatusState {
    object Loading : AdminUserStatusState()
    data class Success(val message: String) : AdminUserStatusState()
    data class Error(val message: String) : AdminUserStatusState()
}

sealed class AdminCancelDeliveryState {
    object Loading : AdminCancelDeliveryState()
    data class Success(val message: String) : AdminCancelDeliveryState()
    data class Error(val message: String) : AdminCancelDeliveryState()
}