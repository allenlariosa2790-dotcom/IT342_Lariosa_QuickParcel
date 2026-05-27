package com.quickparcel.app.features.sender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SenderViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = SenderRepository(retrofitClient)

    private val _dashboardResult = MutableSharedFlow<DashboardState>()
    val dashboardResult = _dashboardResult.asSharedFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardResult.emit(DashboardState.Loading)
            val result = repository.getDashboardData()
            when (result) {
                is SenderModels.DashboardResult.Success -> {
                    _dashboardResult.emit(DashboardState.Success(result.stats, result.recentDeliveries))
                }
                is SenderModels.DashboardResult.Error -> {
                    _dashboardResult.emit(DashboardState.Error(result.message))
                }
            }
        }
    }
}

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val stats: SenderModels.DashboardStats, val recentDeliveries: List<com.quickparcel.app.shared.models.Delivery>) : DashboardState()
    data class Error(val message: String) : DashboardState()
}