package com.mikhail.vortex.model

import com.google.gson.annotations.SerializedName

data class WatchlistResponse(
    val code: String = "",
    val data: WatchlistData = WatchlistData()
)

data class WatchlistData(
    val futures: List<WatchlistItem> = emptyList(),
    val spot: List<WatchlistItem> = emptyList(),
    val all: List<WatchlistItem> = emptyList(),
    val count: Int = 0
)

data class WatchlistItem(
    val symbol: String = "",
    val price: Double = 0.0,
    val market: String = "",
    val side: String = "",
    val score: Int = 0,
    @SerializedName("setup_type") val setupType: String = "-",
    @SerializedName("args_text") val argsText: String = "",
    val status: String = "",
    @SerializedName("waiting_for") val waitingFor: String = "",
    @SerializedName("trigger_price") val triggerPrice: Double = 0.0,
    @SerializedName("invalidation_price") val invalidationPrice: Double = 0.0,
    @SerializedName("created_at") val createdAt: Double = 0.0,
    @SerializedName("updated_at") val updatedAt: Double = 0.0,
    @SerializedName("expires_at") val expiresAt: Double = 0.0,
    @SerializedName("expires_in_sec") val expiresInSec: Int = 0,
    val confirmed: Boolean = false,
    @SerializedName("confirmation_reason") val confirmationReason: String = ""
)
