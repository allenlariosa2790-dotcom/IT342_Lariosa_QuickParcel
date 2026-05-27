package com.quickparcel.app.features.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.models.ParcelRequest
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DeliveryViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = DeliveryRepository(retrofitClient)

    private val _distanceResult = MutableSharedFlow<DistanceState>()
    val distanceResult = _distanceResult.asSharedFlow()

    private val _createResult = MutableSharedFlow<CreateDeliveryState>()
    val createResult = _createResult.asSharedFlow()

    fun calculateDistance(pickupAddress: String, dropoffAddress: String, weight: Double) {
        viewModelScope.launch {
            _distanceResult.emit(DistanceState.Loading)
            val result = repository.calculateDistance(pickupAddress, dropoffAddress, weight)
            when (result) {
                is DeliveryModels.DistanceResult.Success -> {
                    _distanceResult.emit(DistanceState.Success(result.distance, result.estimatedCost))
                }
                is DeliveryModels.DistanceResult.Error -> {
                    _distanceResult.emit(DistanceState.Error(result.message))
                }
            }
        }
    }

    fun createDelivery(
        parcelName: String,
        parcelDescription: String?,
        parcelWeight: Double,
        parcelSize: String,
        parcelCategory: String,
        isFragile: Boolean,
        pickupAddress: String,
        dropoffAddress: String,
        notes: String?,
        scheduledTime: String?,
        paymentMethod: String,
        pickupLat: Double? = null,
        pickupLng: Double? = null,
        dropoffLat: Double? = null,
        dropoffLng: Double? = null
    ) {
        viewModelScope.launch {
            _createResult.emit(CreateDeliveryState.Loading)

            val parcelRequest = ParcelRequest(
                name = parcelName,
                description = parcelDescription,
                weight = parcelWeight,
                size = parcelSize,
                category = parcelCategory,
                isFragile = isFragile
            )

            val request = DeliveryModels.CreateDeliveryRequest(
                parcel = parcelRequest,
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                pickupLatitude = pickupLat,
                pickupLongitude = pickupLng,
                dropoffLatitude = dropoffLat,
                dropoffLongitude = dropoffLng,
                notes = notes,
                scheduledTime = scheduledTime,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "COD") "PENDING" else "UNPAID"
            )

            val result = repository.createDelivery(request)
            when (result) {
                is DeliveryModels.CreateDeliveryResult.Success -> {
                    _createResult.emit(CreateDeliveryState.Success(result.delivery))
                }
                is DeliveryModels.CreateDeliveryResult.Error -> {
                    _createResult.emit(CreateDeliveryState.Error(result.message))
                }
            }
        }
    }
}

sealed class DistanceState {
    object Loading : DistanceState()
    data class Success(val distance: Double, val estimatedCost: Double) : DistanceState()
    data class Error(val message: String) : DistanceState()
}

sealed class CreateDeliveryState {
    object Loading : CreateDeliveryState()
    data class Success(val delivery: com.quickparcel.app.shared.models.Delivery) : CreateDeliveryState()
    data class Error(val message: String) : CreateDeliveryState()
}