package com.quickparcel.app.features.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.models.Delivery
import com.quickparcel.app.shared.models.TrackingHistory
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class TrackingViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = TrackingRepository(retrofitClient)

    private val _deliveriesResult = MutableSharedFlow<TrackingState>()
    val deliveriesResult = _deliveriesResult.asSharedFlow()

    private val _deliveryDetailsResult = MutableSharedFlow<TrackingState>()
    val deliveryDetailsResult = _deliveryDetailsResult.asSharedFlow()

    private val _historyResult = MutableSharedFlow<TrackingState>()
    val historyResult = _historyResult.asSharedFlow()

    private val _imageResult = MutableSharedFlow<TrackingState>()
    val imageResult = _imageResult.asSharedFlow()

    fun loadMyDeliveries() {
        viewModelScope.launch {
            _deliveriesResult.emit(TrackingState.Loading)
            val result = repository.getMyDeliveries()
            when (result) {
                is TrackingModels.TrackingResult.Success -> {
                    _deliveriesResult.emit(TrackingState.DeliveriesSuccess(result.deliveries, result.stats))
                }
                is TrackingModels.TrackingResult.Error -> {
                    _deliveriesResult.emit(TrackingState.Error(result.message))
                }
            }
        }
    }

    fun loadDeliveryDetails(id: Int) {
        viewModelScope.launch {
            _deliveryDetailsResult.emit(TrackingState.DeliveryDetailsLoading)
            val delivery = repository.getDeliveryById(id)
            if (delivery != null) {
                _deliveryDetailsResult.emit(TrackingState.DeliveryDetailsSuccess(delivery))
            } else {
                _deliveryDetailsResult.emit(TrackingState.Error("Failed to load delivery details"))
            }
        }
    }

    fun loadTrackingHistory(id: Int) {
        viewModelScope.launch {
            val history = repository.getTrackingHistory(id)
            _historyResult.emit(TrackingState.HistorySuccess(history))
        }
    }

    fun loadParcelImage(id: Int) {
        viewModelScope.launch {
            val image = repository.getParcelImage(id)
            if (image != null && image.hasImage) {
                _imageResult.emit(TrackingState.ImageSuccess(image.imageUrl ?: ""))
            } else {
                _imageResult.emit(TrackingState.ImageError)
            }
        }
    }
}

sealed class TrackingState {
    object Loading : TrackingState()
    data class DeliveriesSuccess(val deliveries: List<Delivery>, val stats: TrackingModels.DeliveryStats) : TrackingState()
    object DeliveryDetailsLoading : TrackingState()
    data class DeliveryDetailsSuccess(val delivery: Delivery) : TrackingState()
    data class HistorySuccess(val history: List<TrackingHistory>) : TrackingState()
    data class ImageSuccess(val imageUrl: String) : TrackingState()
    object ImageError : TrackingState()
    data class Error(val message: String) : TrackingState()
}