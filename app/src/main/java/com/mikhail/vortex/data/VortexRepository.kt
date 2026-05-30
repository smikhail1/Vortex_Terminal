package com.mikhail.vortex.data

import com.mikhail.vortex.api.ApiClient
import com.mikhail.vortex.api.ApiService
import com.mikhail.vortex.model.DashboardResponse
import com.mikhail.vortex.model.HealthResponse
import com.mikhail.vortex.model.HistoryRow
import com.mikhail.vortex.model.IntelligenceResponse
import com.mikhail.vortex.model.SpotPlannerResponse
import com.mikhail.vortex.model.WatchlistResponse

class VortexRepository(
    private val api: ApiService = ApiClient.api
) {
    suspend fun getDashboard(): DashboardResponse = api.getDashboard()

    suspend fun getHealth(): HealthResponse = api.getHealth()

    suspend fun getHistory(): List<HistoryRow> = api.getHistory()

    suspend fun getSpotPlanner(): SpotPlannerResponse = api.getSpotPlanner()

    suspend fun getWatchlist(): WatchlistResponse = api.getWatchlist()

    suspend fun getIntelligence(): IntelligenceResponse = api.getIntelligence()
}

object VortexRepositoryProvider {
    val default: VortexRepository by lazy { VortexRepository() }
}
