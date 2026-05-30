package com.mikhail.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhail.vortex.data.VortexRepository
import com.mikhail.vortex.data.VortexRepositoryProvider
import com.mikhail.vortex.model.IntelligenceResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IntelligenceUiState(
    val data: IntelligenceResponse? = null,
    val error: String = ""
)

class IntelligenceViewModel(
    private val repository: VortexRepository = VortexRepositoryProvider.default
) : ViewModel() {
    private val _uiState = MutableStateFlow(IntelligenceUiState())
    val uiState: StateFlow<IntelligenceUiState> = _uiState

    private var autoRefreshStarted = false

    fun startAutoRefresh(intervalMs: Long = 5000L) {
        if (autoRefreshStarted) return
        autoRefreshStarted = true

        viewModelScope.launch {
            while (true) {
                refresh()
                delay(intervalMs)
            }
        }
    }

    private suspend fun refresh() {
        try {
            _uiState.value = IntelligenceUiState(data = repository.getIntelligence())
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message ?: "connection error")
        }
    }
}
