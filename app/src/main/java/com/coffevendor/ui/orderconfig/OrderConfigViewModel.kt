package com.coffevendor.ui.orderconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffevendor.data.model.*
import com.coffevendor.data.remote.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderConfigUiState {
    data object Idle : OrderConfigUiState()
    data object Loading : OrderConfigUiState()
    data class Success(val order: Order) : OrderConfigUiState()
    data class Error(val message: String) : OrderConfigUiState()
}

@HiltViewModel
class OrderConfigViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<OrderConfigUiState>(OrderConfigUiState.Idle)
    val uiState: StateFlow<OrderConfigUiState> = _uiState.asStateFlow()

    fun placeOrder(request: CreateOrderRequest) {
        viewModelScope.launch {
            _uiState.value = OrderConfigUiState.Loading
            try {
                val response = ApiClient.apiService.createOrder(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.order?.let { order ->
                        _uiState.value = OrderConfigUiState.Success(order)
                    } ?: run {
                        _uiState.value = OrderConfigUiState.Error("No order data returned")
                    }
                } else {
                    _uiState.value = OrderConfigUiState.Error(
                        response.body()?.message ?: "Failed to place order"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = OrderConfigUiState.Error(
                    e.message ?: "Network error occurred"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = OrderConfigUiState.Idle
    }
}
