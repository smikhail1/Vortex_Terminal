package com.mikhail.vortex.api

import com.mikhail.vortex.BuildConfig
import com.mikhail.vortex.model.DashboardResponse
import com.mikhail.vortex.model.HealthResponse
import com.mikhail.vortex.model.HistoryRow
import com.mikhail.vortex.model.SpotPlannerResponse
import com.mikhail.vortex.model.IntelligenceResponse
import com.mikhail.vortex.model.MarketPulseResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.mikhail.vortex.model.WatchlistResponse

interface ApiService {
    @GET("api/dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/mobile_history")
    suspend fun getHistory(): List<HistoryRow>

    @GET("api/spot-planner")
    suspend fun getSpotPlanner(): SpotPlannerResponse

    @GET("api/watchlist")
    suspend fun getWatchlist(): WatchlistResponse

    @GET("api/intelligence")
    suspend fun getIntelligence(): IntelligenceResponse

    @GET("api/analytics/market-pulse")
    suspend fun getMarketPulse(): MarketPulseResponse
}

object ApiClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.VORTEX_HTTP_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
