package com.mikhail.vortex.model

data class DashboardResponse(
    val market: Market = Market(),
    val account: Account = Account(),
    val positions: Positions = Positions(),
    val system: SystemData = SystemData(),
    val terminal: TerminalData = TerminalData(),
    val today: TodaySummary = TodaySummary(),
    val portfolio: PortfolioSummary = PortfolioSummary(),
    val context_fusion: ContextFusionBlock = ContextFusionBlock(),
    val macro_regime: MacroRegimeBlock? = null
)

data class Market(
    val prices: Map<String, Double> = emptyMap(),
    val ta_data: Map<String, TAData> = emptyMap(),
    val last_market_update_ts: Double? = null,
    val last_ta_update_ts: Double? = null
)

data class TAData(
    val price: Double = 0.0,
    val rsi: Double = 0.0,
    val rsi_main: Double = 0.0,
    val atr_pct: Double = 0.0,
    val dist_to_support: Double = 0.0,
    val dist_to_resistance: Double = 0.0,
    val atr: Double = 0.0,
    val imbalance: Double = 1.0,
    val trend_4h: String = "ranging",
    val trend_bias_30m: String = "neutral",
    val market_regime: String = "mixed",
    val ema20: Double = 0.0,
    val ema50: Double = 0.0,
    val vol_ratio: Double = 0.0,
    val vol_spike: Boolean = false,
    val near_support: Boolean = false,
    val near_resistance: Boolean = false,
    val breakout: Boolean = false,
    val breakout_dir: String = "none",
    val breakout_level: Double = 0.0,
    val pullback_long_ready: Boolean = false,
    val pullback_short_ready: Boolean = false,
    val retest_long_ready: Boolean = false,
    val retest_short_ready: Boolean = false,
    val setup_zone: String = "none"
)

data class Account(
    val balances: Balances = Balances()
)

data class Balances(
    val fut: Double = 0.0,
    val spot: Double = 0.0
)

data class Positions(
    val fut: Map<String, Position> = emptyMap(),
    val spot: Map<String, Position> = emptyMap()
)

data class Position(
    val entry: Double? = null,
    val avg_price: Double? = null,
    val sl: Double? = null,
    val tp: Double? = null,
    val tp1: Double? = null,
    val tp2: Double? = null,
    val side: String? = null,
    val leverage: Double? = null,
    val liq_price: Double? = null,
    val pnl: Double? = null,
    val pnl_net: Double? = null,
    val fills_count: Int? = null,
    val tp1_hit: Boolean? = null,
    val breakeven: Boolean? = null,
    val status_label: String? = null
)

data class SystemData(
    val uptime: String = "",
    val ram_mb: String = "",
    val ping_ms: String = "",
    val sys_logs: List<String> = emptyList(),
    val fut_pool: List<String> = emptyList(),
    val spot_pool: List<String> = emptyList(),
    val rotation_timer: Int = 0,
    val macro: MacroData = MacroData()
)

data class MacroData(
    val btc_trend: String = "neutral",
    val global_filter: String = "allow_all",
    val binance_btc: Double = 0.0,
    val oi_amount: Double = 0.0,
    val fng_value: Int = 50
)

data class MacroRegimeBlock(
    val regime: String? = null,
    val confidence: Int? = null,
    val recommendation: MacroRecommendation? = null,
    val reasons: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

data class MacroRecommendation(
    val long_permission: String? = null,
    val short_permission: String? = null,
    val risk_mode: String? = null
)

data class TerminalData(
    val rotation_timer: Int = 0,
    val futures_cards: List<TerminalCard> = emptyList(),
    val spot_cards: List<TerminalCard> = emptyList()
)

data class TerminalCard(
    val symbol: String = "",
    val price: Double = 0.0,
    val status: String = "watch",
    val indicator: String = "gray",
    val score: Int? = null,
    val args_text: String = "",
    val setup_type: String? = null,
    val position: Position? = null
)

data class TodaySummary(
    val today_realized_fut: Double = 0.0,
    val today_realized_spot: Double = 0.0,
    val today_total_realized: Double = 0.0,
    val today_open_fut: Double = 0.0,
    val today_open_spot: Double = 0.0,
    val today_total_open: Double = 0.0
)

data class PortfolioSummary(
    val spot_free: Double = 0.0,
    val spot_equity: Double = 0.0,
    val fut_free: Double = 0.0,
    val fut_equity: Double = 0.0,
    val total_equity: Double = 0.0
)

data class ContextFusionBlock(
    val available: Boolean = false,
    val summary: ContextFusionSummary? = null,
    val symbols: List<ContextFusionSymbol> = emptyList(),
    val important: List<ContextFusionSymbol> = emptyList()
)

data class ContextFusionSummary(
    val symbols_count: Int? = null,
    val final_view_counts: Map<String, Int>? = null,
    val heatmap_bias: String? = null,
    val heatmap_net_bias_score: Double? = null
)

data class ContextFusionSymbol(
    val symbol: String = "",
    val strategy_state: String? = null,
    val side: String? = null,
    val strategy: FusionStrategy? = null,
    val policy: FusionPolicy? = null,
    val ea: FusionEa? = null,
    val setup_zone: FusionSetupZone? = null,
    val heatmap: FusionHeatmap? = null,
    val ichimoku: IchimokuContext? = null,
    val final: FusionFinal? = null
)

data class IchimokuContext(
    val available: Boolean = false,
    val trend_bias: String? = null,
    val cloud_state: String? = null,
    val tk_state: String? = null,
    val cloud_bias: String? = null,
    val support_status: String? = null,
    val quality: Int? = null,
    val warnings: List<String> = emptyList()
)

data class FusionFinal(
    val view: String? = null,
    val score: Int? = null,
    val reasons: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val blockers: List<String> = emptyList()
)

data class FusionSetupZone(
    val preferred_zone: String? = null,
    val zone_quality: Int? = null,
    val support_status: String? = null,
    val range_position_pct: Double? = null,
    val near_support: Boolean? = null,
    val near_resistance: Boolean? = null,
    val near_ema20: Boolean? = null,
    val warnings: List<String> = emptyList()
)

data class FusionHeatmap(
    val global: FusionHeatmapGlobal? = null,
    val local_bias: String? = null,
    val trend_4h: String? = null,
    val adx: Double? = null,
    val rsi_main: Double? = null,
    val vol_ratio: Double? = null
)

data class FusionHeatmapGlobal(
    val bias: String? = null,
    val net_bias_score: Double? = null,
    val support_status: String? = null,
    val support_score: Int? = null
)

data class FusionPolicy(
    val code: String? = null,
    val reason: String? = null
)

data class FusionStrategy(
    val score: Int? = null,
    val setup_type: String? = null,
    val args_text: String? = null,
    val blocked_reason: String? = null
)

data class FusionEa(
    val present: Boolean? = null,
    val grade: String? = null,
    val score: Int? = null,
    val label: String? = null,
    val raw: String? = null
)

data class HealthResponse(
    val status: String = "offline",
    val mode: String = "PAPER",
    val uptime: String = "",
    val ping_ms: String = "0",
    val market_age_sec: Double = 9999.0,
    val ta_age_sec: Double = 9999.0,
    val server_time: Long = 0L
)

data class HistoryRow(
    val timestamp: String = "",
    val symbol: String = "",
    val side: String = "",
    val type: String = "",
    val setup_type: String = "",
    val args_text: String = "",
    val entry_price: String = "",
    val target_tp: String = "",
    val exit_price: String = "",
    val pnl: String = "",
    val status: String = ""
)
