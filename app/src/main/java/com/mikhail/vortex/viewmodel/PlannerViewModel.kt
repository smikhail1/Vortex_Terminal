package com.mikhail.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhail.vortex.api.ApiClient
import com.mikhail.vortex.model.SpotIdea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlannerViewModel : ViewModel() {

    private val _ideas = MutableStateFlow<List<SpotIdea>>(emptyList())
    val ideas: StateFlow<List<SpotIdea>> = _ideas

    fun loadPlanner() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getSpotPlanner()
                _ideas.value = response.spot_ideas
            } catch (_: Exception) {
            }
        }
    }
}