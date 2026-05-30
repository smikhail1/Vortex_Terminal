package com.mikhail.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhail.vortex.data.VortexRepository
import com.mikhail.vortex.data.VortexRepositoryProvider
import com.mikhail.vortex.model.HistoryRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: VortexRepository = VortexRepositoryProvider.default
) : ViewModel() {

    private val _trades = MutableStateFlow<List<HistoryRow>>(emptyList())
    val trades: StateFlow<List<HistoryRow>> = _trades

    private var started = false

    fun start() {
        if (started) return
        started = true

        viewModelScope.launch {
            while (true) {
                try {
                    _trades.value = repository.getHistory()
                } catch (_: Exception) {
                }
                delay(4000)
            }
        }
    }
}
