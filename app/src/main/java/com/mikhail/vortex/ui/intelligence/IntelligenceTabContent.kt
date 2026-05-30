package com.mikhail.vortex.ui.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mikhail.vortex.model.IntelligenceResponse
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikhail.vortex.viewmodel.IntelligenceViewModel

private val Panel = Color(0xFF171B22)
private val PanelSoft = Color(0xFF1C212A)
private val Divider = Color(0xFF2A313D)
private val Txt = Color(0xFFF2F4F8)
private val TxtSoft = Color(0xFFAFB8C5)
private val Blue = Color(0xFF5DA9FF)
private val Green = Color(0xFF31C26A)
private val Red = Color(0xFFFF5E57)
private val Yellow = Color(0xFFFFC245)
private val Purple = Color(0xFFB978FF)

@Composable
fun IntelligenceTabContent(
    serverStatus: String,
    serverDot: String
) {
    val vm: IntelligenceViewModel = viewModel()
    val uiState by vm.uiState.collectAsState()
    val data = uiState.data
    val error = uiState.error

    LaunchedEffect(Unit) {
        vm.startAutoRefresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IntelligenceHeader(
                serverStatus = serverStatus,
                serverDot = serverDot,
                mode = data?.mode ?: "PAPER",
                schema = data?.schema_version ?: "-"
            )
        }

        if (error.isNotBlank()) {
            item {
                IntelligenceCard(title = "Connection", accent = Red) {
                    Text("Ошибка загрузки Intelligence: $error", color = TxtSoft, fontSize = 13.sp)
                }
            }
        }

        item { AvailabilityCard(data) }
        item { OutcomeCard(data?.outcome_summary) }
        item { AdaptiveCandidateCard(data?.adaptive_be_candidates) }
        item { ShadowSimulationCard(data?.shadow_policy_simulation) }
        item { PolicyCard(data?.policy_recommendations) }
    }
}

@Composable
private fun IntelligenceHeader(serverStatus: String, serverDot: String, mode: String, schema: String) {
    IntelligenceCard(title = "🧠 VORTEX INTELLIGENCE", accent = Purple) {
        Text("STATUS: $serverStatus | MODE: $mode", color = statusColor(serverDot), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text("Analytics schema: $schema", color = TxtSoft, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text("Shadow analytics only. Реальная торговля не меняется.", color = TxtSoft, fontSize = 12.sp)
    }
}

@Composable
private fun AvailabilityCard(data: IntelligenceResponse?) {
    val available = data?.available ?: emptyMap()
    IntelligenceCard(title = "Runtime Availability", accent = Blue) {
        if (available.isEmpty()) {
            Text("Нет данных availability", color = TxtSoft, fontSize = 13.sp)
        } else {
            available.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { item ->
                        MiniMetric(
                            title = item.key.replace("_", " "),
                            value = if (item.value) "OK" else "MISS",
                            color = if (item.value) Green else Red,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OutcomeCard(summary: JsonObject?) {
    val byReason = summary.obj("by_close_reason")
    val bu = byReason.obj("BU")
    val stall = byReason.obj("STALL")
    val sl = byReason.obj("SL")

    IntelligenceCard(title = "Outcome Intelligence", accent = Green) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("BU winrate", "${bu.d("winrate_pct").fmt()}%", if (bu.d("winrate_pct") > 0.0) Green else Red, Modifier.weight(1f))
            MiniMetric("BU avg pnl", bu.d("avg_pnl_net").fmt4(), if (bu.d("avg_pnl_net") >= 0) Green else Red, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("STALL winrate", "${stall.d("winrate_pct").fmt()}%", if (stall.d("winrate_pct") >= 50.0) Green else Yellow, Modifier.weight(1f))
            MiniMetric("STALL avg pnl", stall.d("avg_pnl_net").fmt4(), if (stall.d("avg_pnl_net") >= 0) Green else Red, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        MiniMetric("SL avg pnl", sl.d("avg_pnl_net").fmt4(), Red, Modifier.fillMaxWidth())
    }
}

@Composable
private fun AdaptiveCandidateCard(candidatesRoot: JsonObject?) {
    val candidates = candidatesRoot.obj("candidates")
    val momentumLong = candidates.obj("momentum_long")
    val best = momentumLong.obj("best_candidate")
    val sample = momentumLong.obj("sample")

    IntelligenceCard(title = "Best Adaptive Candidate", accent = Yellow) {
        Text("Momentum Long", color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("Best delay", "${best.i("delay_sec")} sec", Blue, Modifier.weight(1f))
            MiniMetric("Score", best.d("score").fmt(), Green, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("Confidence", best.s("confidence", "low").uppercase(), Yellow, Modifier.weight(1f))
            MiniMetric("Sample", sample.i("count").toString(), TxtSoft, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text(best.reasonsText(), color = TxtSoft, fontSize = 12.sp)
    }
}

@Composable
private fun ShadowSimulationCard(root: JsonObject?) {
    val overall = root.obj("overall_best")
    val bySetup = root.obj("by_setup")
    val momentum = bySetup.obj("momentum_long")

    IntelligenceCard(title = "Shadow Policy Simulation", accent = Purple) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("Real PnL", overall.d("real_pnl").fmt4(), TxtSoft, Modifier.weight(1f))
            MiniMetric("Shadow PnL", overall.d("best_candidate_pnl").fmt4(), Green, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("Delta", overall.d("delta").fmt4(), if (overall.d("delta") >= 0) Green else Red, Modifier.weight(1f))
            MiniMetric("ML best delay", "${momentum.i("best_delay_sec")} sec", Blue, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PolicyCard(root: JsonObject?) {
    val recs = root.obj("recommendations")
    IntelligenceCard(title = "Policy Recommendations", accent = Blue) {
        if (recs.entrySet().isEmpty()) {
            Text("Пока нет рекомендаций", color = TxtSoft, fontSize = 13.sp)
        } else {
            recs.entrySet().take(5).forEach { entry ->
                val item = entry.value.asJsonObjectSafe()
                Text(entry.key, color = Txt, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = item.s("recommendation", "observe_only") + " | confidence: " + item.s("confidence", "low"),
                    color = TxtSoft,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Divider)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun IntelligenceCard(title: String, accent: Color, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(accent, RoundedCornerShape(99.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                Text(title, color = Txt, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MiniMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PanelSoft),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TxtSoft, fontSize = 11.sp)
            Spacer(Modifier.height(5.dp))
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

private fun statusColor(dot: String): Color = when (dot) {
    "green" -> Green
    "yellow" -> Yellow
    "red" -> Red
    else -> TxtSoft
}

private fun JsonObject?.obj(key: String): JsonObject {
    if (this == null) return JsonObject()
    val e = this.get(key) ?: return JsonObject()
    return e.asJsonObjectSafe()
}

private fun JsonElement?.asJsonObjectSafe(): JsonObject {
    return try {
        if (this != null && this.isJsonObject) this.asJsonObject else JsonObject()
    } catch (_: Exception) {
        JsonObject()
    }
}

private fun JsonObject?.d(key: String): Double {
    return try { this?.get(key)?.asDouble ?: 0.0 } catch (_: Exception) { 0.0 }
}

private fun JsonObject?.i(key: String): Int {
    return try { this?.get(key)?.asInt ?: 0 } catch (_: Exception) { 0 }
}

private fun JsonObject?.s(key: String, def: String = ""): String {
    return try { this?.get(key)?.asString ?: def } catch (_: Exception) { def }
}

private fun Double.fmt(): String = String.format("%.1f", this)
private fun Double.fmt4(): String = String.format("%.4f", this)

private fun JsonObject?.reasonsText(): String {
    return try {
        val arr = this?.getAsJsonArray("reasons") ?: return "reasons: -"
        "reasons: " + arr.joinToString(", ") { it.asString }
    } catch (_: Exception) {
        "reasons: -"
    }
}
