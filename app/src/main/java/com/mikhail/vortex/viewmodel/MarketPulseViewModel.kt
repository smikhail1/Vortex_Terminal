package com.mikhail.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhail.vortex.api.ApiClient
import com.mikhail.vortex.model.MarketPulseResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MarketPulseUiState(
    val loading: Boolean = true,
    val data: MarketPulseResponse? = null,
    val error: String? = null
)

class MarketPulseViewModel : ViewModel() {
    private val _state = MutableStateFlow(MarketPulseUiState())
    val state: StateFlow<MarketPulseUiState> = _state

    fun load() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getMarketPulse()
                _state.value = MarketPulseUiState(
                    loading = false,
                    data = response,
                    error = if (response.ok) null else "Сервер вернул неполную сводку"
                )
            } catch (e: Exception) {
                _state.value = MarketPulseUiState(
                    loading = false,
                    data = _state.value.data,
                    error = e.message ?: "Не удалось получить сводку"
                )
            }
        }
    }
}
