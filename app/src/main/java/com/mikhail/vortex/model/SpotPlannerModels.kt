package com.mikhail.vortex.model

data class SpotPlannerResponse(
    val spot_ideas: List<SpotIdea> = emptyList(),
    val last_update_ts: Long = 0L
)

data class SpotIdea(
    val symbol: String = "",
    val tier: String = "",
    val score: Int = 0,
    val priority_rank: Int = 0,
    val confidence_score: Int = 0,
    val confidence_band: String = "",
    val horizon: String = "",
    val risk_grade: String = "",
    val status: String = "",
    val readiness: String = "",
    val rr_ratio: Double = 0.0,
    val current_price: Double = 0.0,
    val accumulation_zone: AccumulationZone = AccumulationZone(),
    val avg_entry: Double = 0.0,
    val entries: List<PlannerEntry> = emptyList(),
    val targets: List<PlannerTarget> = emptyList(),
    val invalidation: Double = 0.0,
    val expected_return_base_pct: Double = 0.0,
    val expected_return_bull_pct: Double = 0.0,
    val trend_d1: String = "",
    val trend_w1: String = "",
    val structure_4h: String = "",
    val action_label: String = "",
    val action_hint: String = "",
    val thesis: List<String> = emptyList()
)

data class AccumulationZone(
    val top: Double = 0.0,
    val bottom: Double = 0.0
)

data class PlannerEntry(
    val allocation_pct: Int = 0,
    val price: Double = 0.0
)

data class PlannerTarget(
    val price: Double = 0.0,
    val close_pct: Int = 0
)