package com.mikhail.vortex

import com.mikhail.vortex.model.AccumulationZone
import com.mikhail.vortex.model.SpotIdea
import com.mikhail.vortex.ui.planner.PlannerSignals
import org.junit.Assert.assertEquals
import org.junit.Test

class PlannerSignalsTest {
    @Test
    fun comparator_prioritizesReadyIdeaInsideZone() {
        val earlyHighConfidence = idea(
            symbol = "EARLY",
            readiness = "LOW",
            confidence = 95,
            price = 120.0,
            zoneTop = 100.0,
            zoneBottom = 95.0,
            priority = 1
        )
        val readyInsideZone = idea(
            symbol = "READY",
            readiness = "HIGH",
            confidence = 70,
            price = 98.0,
            zoneTop = 100.0,
            zoneBottom = 95.0,
            priority = 2
        )

        val sorted = listOf(earlyHighConfidence, readyInsideZone)
            .sortedWith(PlannerSignals.comparator())

        assertEquals("READY", sorted.first().symbol)
    }

    @Test
    fun zoneDistanceRank_handlesInsideNearAndFarPrices() {
        assertEquals(0, PlannerSignals.zoneDistanceRank(idea(price = 98.0)))
        assertEquals(1, PlannerSignals.zoneDistanceRank(idea(price = 100.5)))
        assertEquals(3, PlannerSignals.zoneDistanceRank(idea(price = 120.0)))
    }

    @Test
    fun riskRank_supportsEnglishAndRussianLabels() {
        assertEquals(0, PlannerSignals.riskRank("Low risk"))
        assertEquals(0, PlannerSignals.riskRank("Низкий риск"))
        assertEquals(1, PlannerSignals.riskRank("Средний риск"))
        assertEquals(2, PlannerSignals.riskRank("High risk"))
    }

    private fun idea(
        symbol: String = "BTCUSDT",
        readiness: String = "HIGH",
        confidence: Int = 80,
        risk: String = "Low risk",
        price: Double = 98.0,
        zoneTop: Double = 100.0,
        zoneBottom: Double = 95.0,
        priority: Int = 1
    ): SpotIdea {
        return SpotIdea(
            symbol = symbol,
            readiness = readiness,
            confidence_score = confidence,
            risk_grade = risk,
            current_price = price,
            accumulation_zone = AccumulationZone(top = zoneTop, bottom = zoneBottom),
            priority_rank = priority
        )
    }
}
