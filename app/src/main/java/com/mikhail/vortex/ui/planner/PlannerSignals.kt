package com.mikhail.vortex.ui.planner

import com.mikhail.vortex.model.SpotIdea

object PlannerSignals {
    fun comparator(): Comparator<SpotIdea> {
        return compareBy<SpotIdea> { readinessRank(it.readiness) }
            .thenBy { zoneDistanceRank(it) }
            .thenByDescending { it.confidence_score }
            .thenBy { riskRank(it.risk_grade) }
            .thenBy { it.priority_rank }
    }

    fun readinessRank(readiness: String): Int {
        val r = readiness.lowercase()
        return when {
            "ready" in r || "готов" in r || "high" in r || "высок" in r -> 0
            "soon" in r || "скоро" in r || "mid" in r || "сред" in r -> 1
            "early" in r || "рано" in r || "low" in r || "низ" in r -> 2
            else -> 3
        }
    }

    fun zoneDistanceRank(idea: SpotIdea): Int {
        val price = idea.current_price
        val top = idea.accumulation_zone.top
        val bottom = idea.accumulation_zone.bottom
        if (price <= 0.0 || top <= 0.0 || bottom <= 0.0) return 3
        val high = maxOf(top, bottom)
        val low = minOf(top, bottom)
        return when {
            price in low..high -> 0
            price > high -> distanceRank((price - high) / price)
            else -> distanceRank((low - price) / price)
        }
    }

    fun riskRank(risk: String): Int {
        val r = risk.lowercase()
        return when {
            "низ" in r || "low" in r -> 0
            "сред" in r || "medium" in r -> 1
            "выс" in r || "high" in r -> 2
            else -> 3
        }
    }

    private fun distanceRank(fraction: Double): Int {
        val percent = fraction * 100.0
        return when {
            percent <= 1.0 -> 1
            percent <= 3.0 -> 2
            else -> 3
        }
    }
}
