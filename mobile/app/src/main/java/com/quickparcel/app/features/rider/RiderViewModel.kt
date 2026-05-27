package com.quickparcel.app.features.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RiderViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = RiderRepository(retrofitClient)

    private val _availableResult = MutableSharedFlow<RiderAvailableState>()
    val availableResult = _availableResult.asSharedFlow()

    private val _acceptResult = MutableSharedFlow<RiderAcceptState>()
    val acceptResult = _acceptResult.asSharedFlow()

    private val _statusResult = MutableSharedFlow<RiderStatusState>()
    val statusResult = _statusResult.asSharedFlow()

    private val _dashboardResult = MutableSharedFlow<RiderDashboardState>()
    val dashboardResult = _dashboardResult.asSharedFlow()

    fun loadAvailableDeliveries() {
        viewModelScope.launch {
            _availableResult.emit(RiderAvailableState.Loading)
            val result = repository.getAvailableDeliveries()
            when (result) {
                is RiderModels.AvailableResult.Success -> {
                    _availableResult.emit(RiderAvailableState.Success(result.deliveries))
                }
                is RiderModels.AvailableResult.Error -> {
                    _availableResult.emit(RiderAvailableState.Error(result.message))
                }
            }
        }
    }

    fun acceptDelivery(deliveryId: Int) {
        viewModelScope.launch {
            _acceptResult.emit(RiderAcceptState.Loading)
            val result = repository.acceptDelivery(deliveryId)
            when (result) {
                is RiderModels.AcceptResult.Success -> {
                    _acceptResult.emit(RiderAcceptState.Success(result.delivery))
                }
                is RiderModels.AcceptResult.Error -> {
                    _acceptResult.emit(RiderAcceptState.Error(result.message))
                }
            }
        }
    }

    fun updateDeliveryStatus(deliveryId: Int, status: String, location: String) {
        viewModelScope.launch {
            _statusResult.emit(RiderStatusState.Loading)
            val result = repository.updateDeliveryStatus(deliveryId, status, location)
            when (result) {
                is RiderModels.StatusResult.Success -> {
                    _statusResult.emit(RiderStatusState.Success(result.delivery))
                }
                is RiderModels.StatusResult.Error -> {
                    _statusResult.emit(RiderStatusState.Error(result.message))
                }
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardResult.emit(RiderDashboardState.Loading)

            val deliveries = repository.getMyDeliveries()
            val activeDelivery = repository.getActiveDelivery()

            val completed = deliveries.filter { it.status == "DELIVERED" }
            val totalEarnings = completed.sumOf { it.estimatedCost }

            val today = java.util.Calendar.getInstance()
            today.set(java.util.Calendar.HOUR_OF_DAY, 0)
            today.set(java.util.Calendar.MINUTE, 0)
            today.set(java.util.Calendar.SECOND, 0)

            val todayEarnings = completed.filter {
                try {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        .parse(it.createdAt)
                    date != null && date.after(today.time)
                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.estimatedCost }

            val weekAgo = java.util.Calendar.getInstance()
            weekAgo.add(java.util.Calendar.DAY_OF_YEAR, -7)
            val weekEarnings = completed.filter {
                try {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        .parse(it.createdAt)
                    date != null && date.after(weekAgo.time)
                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.estimatedCost }

            val twoWeeksAgo = java.util.Calendar.getInstance()
            twoWeeksAgo.add(java.util.Calendar.DAY_OF_YEAR, -14)
            val lastWeekEarnings = completed.filter {
                try {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        .parse(it.createdAt)
                    date != null && date.after(twoWeeksAgo.time) && date.before(weekAgo.time)
                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.estimatedCost }

            val stats = RiderModels.EarningsStats(
                today = todayEarnings,
                thisWeek = weekEarnings,
                lastWeek = lastWeekEarnings,
                total = totalEarnings,
                completedCount = completed.size
            )

            _dashboardResult.emit(RiderDashboardState.Success(stats, activeDelivery, deliveries))
        }
    }
}

sealed class RiderAvailableState {
    object Loading : RiderAvailableState()
    data class Success(val deliveries: List<Delivery>) : RiderAvailableState()
    data class Error(val message: String) : RiderAvailableState()
}

sealed class RiderAcceptState {
    object Loading : RiderAcceptState()
    data class Success(val delivery: Delivery) : RiderAcceptState()
    data class Error(val message: String) : RiderAcceptState()
}

sealed class RiderStatusState {
    object Loading : RiderStatusState()
    data class Success(val delivery: Delivery) : RiderStatusState()
    data class Error(val message: String) : RiderStatusState()
}

sealed class RiderDashboardState {
    object Loading : RiderDashboardState()
    data class Success(
        val stats: RiderModels.EarningsStats,
        val activeDelivery: Delivery?,
        val recentDeliveries: List<Delivery>
    ) : RiderDashboardState()
    data class Error(val message: String) : RiderDashboardState()
}