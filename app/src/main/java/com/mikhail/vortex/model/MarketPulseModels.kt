package com.mikhail.vortex.model

data class MarketPulseResponse(
    val ok: Boolean = false,
    val schema: String = "",
    val ts: Double = 0.0,
    val health: MarketPulseHealth = MarketPulseHealth(),
    val portfolio: MarketPulsePortfolio = MarketPulsePortfolio(),
    val positions: MarketPulsePositions = MarketPulsePositions(),
    val risk: MarketPulseRisk = MarketPulseRisk(),
    val market_regime: MarketPulseRegime = MarketPulseRegime(),
    val context_fusion: MarketPulseFusion = MarketPulseFusion(),
    val watchlist: MarketPulseWatchlist = MarketPulseWatchlist(),
    val near_entries: MarketPulseNearEntries = MarketPulseNearEntries(),
    val pump_advisor: MarketPulsePumpAdvisor = MarketPulsePumpAdvisor(),
    val human_summary: MarketPulseHumanSummary = MarketPulseHumanSummary()
)

data class MarketPulseHealth(
    val status: String = "offline",
    val mode: String = "PAPER",
    val uptime: String = "-",
    val market_age_sec: Double = 9999.0,
    val ta_age_sec: Double = 9999.0,
    val fresh: Boolean = false
)

data class MarketPulsePortfolio(
    val spot_free: Double = 0.0,
    val spot_equity: Double = 0.0,
    val fut_free: Double = 0.0,
    val fut_equity: Double = 0.0,
    val total_equity: Double = 0.0,
    val fut_margin_used: Double = 0.0,
    val fut_notional_open: Double = 0.0,
    val fut_open_pnl: Double = 0.0
)

data class MarketPulsePositions(
    val fut: Map<String, Any?> = emptyMap(),
    val spot: Map<String, Any?> = emptyMap()
)

data class MarketPulseRisk(
    val block_new_entries: Boolean = false,
    val block_reason: String = "",
    val daily_realized_pnl: Double = 0.0,
    val daily_loss_limit_usdt: Double = 0.0,
    val active_symbol_cooldowns: Map<String, Int> = emptyMap(),
    val trades_per_day: Map<String, Int> = emptyMap()
)

data class MarketPulseRegime(
    val available: Boolean = false,
    val regime: String? = null,
    val confidence: Int = 0,
    val risk_mode: String? = null,
    val long_permission: String? = null,
    val short_permission: String? = null,
    val reasons: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val heatmap: Map<String, Any?> = emptyMap(),
    val ichimoku_breadth: Map<String, Any?> = emptyMap(),
    val futures_pressure: Map<String, Any?> = emptyMap(),
    val vortex_pressure: Map<String, Any?> = emptyMap()
)

data class MarketPulseFusion(
    val available: Boolean = false,
    val heatmap_bias: String? = null,
    val heatmap_net_bias_score: Double = 0.0,
    val final_view_counts: Map<String, Int> = emptyMap(),
    val ichimoku: MarketPulseIchimoku = MarketPulseIchimoku()
)

data class MarketPulseIchimoku(
    val above_cloud: Int = 0,
    val below_cloud: Int = 0,
    val inside_cloud: Int = 0,
    val long_supportive: Int = 0,
    val long_against: Int = 0,
    val short_supportive: Int = 0,
    val short_against: Int = 0
)

data class MarketPulseWatchlist(
    val futures: MarketPulseWatchSummary = MarketPulseWatchSummary(),
    val spot: MarketPulseWatchSummary = MarketPulseWatchSummary()
)

data class MarketPulseWatchSummary(
    val len: Int = 0,
    val status_counts: Map<String, Int> = emptyMap(),
    val stage_counts: Map<String, Int> = emptyMap(),
    val setup_types: Map<String, Int> = emptyMap(),
    val sides: Map<String, Int> = emptyMap(),
    val trigger_crossed: Int = 0,
    val would_confirm_now: Int = 0,
    val entry_confirmed: Int = 0,
    val invalidated: Int = 0,
    val confirm_reasons: Map<String, Int> = emptyMap()
)

data class MarketPulseNearEntries(
    val futures: List<MarketPulseNearEntry> = emptyList(),
    val spot: List<MarketPulseNearEntry> = emptyList()
)

data class MarketPulseNearEntry(
    val symbol: String = "",
    val market: String = "",
    val side: String = "",
    val setup_type: String = "",
    val score: Int = 0,
    val dist_pct: Double = 0.0,
    val reason: String = "",
    val stage: String = "",
    val trigger_crossed: Boolean = false,
    val would_confirm_now: Boolean = false,
    val invalidated: Boolean = false
)

data class MarketPulsePumpAdvisor(
    val available: Boolean = false,
    val reason: String? = null,
    val symbols_count: Int = 0,
    val items_len: Int = 0,
    val important_len: Int = 0,
    val phase_counts: Map<String, Int> = emptyMap(),
    val top_important: List<MarketPulsePumpItem> = emptyList()
)

data class MarketPulsePumpItem(
    val symbol: String = "",
    val phase: String = "",
    val score: Int = 0,
    val pump_pct_24h: Double? = null,
    val pump_pct_6h: Double? = null,
    val rsi14: Double? = null,
    val volume_ratio: Double? = null,
    val breakdown_distance_pct: Double? = null,
    val waiting_for: String? = null
)

data class MarketPulseHumanSummary(
    val title: String = "Сводка рынка недоступна",
    val status: String = "",
    val main_text: String = "",
    val why_no_trade: String = "",
    val what_to_watch: List<String> = emptyList(),
    val recommendation: String = ""
)
