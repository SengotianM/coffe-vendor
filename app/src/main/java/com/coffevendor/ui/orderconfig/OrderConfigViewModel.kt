package com.coffevendor.ui.orderconfig

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class OrderConfigUiState {
    data object Idle : OrderConfigUiState()
}

@HiltViewModel
class OrderConfigViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<OrderConfigUiState>(OrderConfigUiState.Idle)
    val uiState: StateFlow<OrderConfigUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = OrderConfigUiState.Idle
    }
}
