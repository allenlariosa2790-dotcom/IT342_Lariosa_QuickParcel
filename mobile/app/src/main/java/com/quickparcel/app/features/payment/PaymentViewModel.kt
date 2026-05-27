package com.quickparcel.app.features.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val repository = PaymentRepository(retrofitClient)

    private val _paymentIntentResult = MutableSharedFlow<PaymentIntentState>()
    val paymentIntentResult = _paymentIntentResult.asSharedFlow()

    private val _paymentStatusResult = MutableSharedFlow<PaymentStatusState>()
    val paymentStatusResult = _paymentStatusResult.asSharedFlow()

    private val _markPaidResult = MutableSharedFlow<MarkPaidState>()
    val markPaidResult = _markPaidResult.asSharedFlow()

    fun createPaymentIntent(deliveryId: Int, amount: Double, description: String) {
        viewModelScope.launch {
            _paymentIntentResult.emit(PaymentIntentState.Loading)
            val result = repository.createStripePaymentIntent(deliveryId, amount, description)
            when (result) {
                is PaymentModels.PaymentResult.Success -> {
                    _paymentIntentResult.emit(PaymentIntentState.Success(result.clientSecret, result.paymentIntentId))
                }
                is PaymentModels.PaymentResult.Error -> {
                    _paymentIntentResult.emit(PaymentIntentState.Error(result.message))
                }
            }
        }
    }

    fun checkPaymentStatus(paymentIntentId: String) {
        viewModelScope.launch {
            _paymentStatusResult.emit(PaymentStatusState.Loading)
            val result = repository.getPaymentStatus(paymentIntentId)
            when (result) {
                is PaymentModels.StatusResult.Success -> {
                    _paymentStatusResult.emit(PaymentStatusState.Success(result.status))
                }
                is PaymentModels.StatusResult.Error -> {
                    _paymentStatusResult.emit(PaymentStatusState.Error(result.message))
                }
            }
        }
    }

    fun markDeliveryAsPaid(deliveryId: Int) {
        viewModelScope.launch {
            _markPaidResult.emit(MarkPaidState.Loading)
            val result = repository.markDeliveryAsPaid(deliveryId)
            when (result) {
                is PaymentModels.MarkPaidResult.Success -> {
                    _markPaidResult.emit(MarkPaidState.Success(result.message))
                }
                is PaymentModels.MarkPaidResult.Error -> {
                    _markPaidResult.emit(MarkPaidState.Error(result.message))
                }
            }
        }
    }
}

sealed class PaymentIntentState {
    object Loading : PaymentIntentState()
    data class Success(val clientSecret: String, val paymentIntentId: String) : PaymentIntentState()
    data class Error(val message: String) : PaymentIntentState()
}

sealed class PaymentStatusState {
    object Loading : PaymentStatusState()
    data class Success(val status: String) : PaymentStatusState()
    data class Error(val message: String) : PaymentStatusState()
}

sealed class MarkPaidState {
    object Loading : MarkPaidState()
    data class Success(val message: String) : MarkPaidState()
    data class Error(val message: String) : MarkPaidState()
}