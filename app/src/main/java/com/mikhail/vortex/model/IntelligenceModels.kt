package com.mikhail.vortex.model

import com.google.gson.JsonObject

data class IntelligenceResponse(
    val schema: String = "",
    val schema_version: String = "",
    val ts: Double = 0.0,
    val mode: String = "PAPER",
    val available: Map<String, Boolean> = emptyMap(),
    val outcome_summary: JsonObject? = null,
    val policy_recommendations: JsonObject? = null,
    val shadow_adaptive_replay: JsonObject? = null,
    val adaptive_be_candidates: JsonObject? = null,
    val shadow_policy_simulation: JsonObject? = null
)
