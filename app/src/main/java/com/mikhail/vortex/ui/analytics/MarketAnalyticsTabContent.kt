package com.mikhail.vortex.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikhail.vortex.model.MarketPulseNearEntry
import com.mikhail.vortex.model.MarketPulsePumpItem
import com.mikhail.vortex.model.MarketPulseResponse
import com.mikhail.vortex.model.MarketPulseWatchSummary
import com.mikhail.vortex.viewmodel.MarketPulseViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

private val Bg = Color(0xFF0E1116)
private val Panel = Color(0xFF171B22)
private val PanelSoft = Color(0xFF1C212A)
private val Divider = Color(0xFF2A313D)
private val Txt = Color(0xFFF2F4F8)
private val TxtSoft = Color(0xFFAFB8C5)
private val Blue = Color(0xFF5DA9FF)
private val Green = Color(0xFF31C26A)
private val Yellow = Color(0xFFFFC245)
private val Orange = Color(0xFFFF9F43)
private val Red = Color(0xFFFF5E57)

@Composable
fun MarketAnalyticsTabContent() {
    val vm: MarketPulseViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            vm.load()
            delay(15_000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .background(Bg)
            .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header(state.loading, state.error, vm::load) }
        val data = state.data
        if (data == null) {
            item { EmptyCard(if (state.loading) "Загружаем рыночную сводку..." else "Сводка пока недоступна.") }
        } else {
            item { HumanSummary(data) }
            item { Overview(data) }
            item { Heading("Индексы рынка") }
            item { Indexes(data) }
            item { WhyNoTrade(data) }
            item { Heading("Фьючерсы") }
            item { WatchSummary(data.watchlist.futures, "FUT") }
            item { Subheading("Ближайшие к входу") }
            if (data.near_entries.futures.isEmpty()) {
                item { EmptyCard("Нет futures-кандидатов с рассчитанной дистанцией.") }
            } else {
                items(data.near_entries.futures.take(15)) { NearEntryRow(it) }
            }
            item { Heading("Spot") }
            item {
                if (data.watchlist.spot.len == 0) EmptyCard("Spot сейчас без активных кандидатов.")
                else WatchSummary(data.watchlist.spot, "SPOT")
            }
            item { Heading("Pump Advisor") }
            item { PumpSummary(data) }
            items(data.pump_advisor.top_important.take(8)) { PumpRow(it) }
            item { EmptyCard("Market Movers: запланировано. Блок будет ловить резкие пампы вне текущего пула.") }
        }
    }
}

@Composable
private fun Header(loading: Boolean, error: String?, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("VORTEX MARKET PULSE", color = Txt, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Сводка рынка и ближайших торговых зон", color = TxtSoft, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = Blue.copy(alpha = 0.20f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(if (loading) "..." else "Обновить", color = Blue, fontSize = 11.sp)
            }
        }
        if (error != null) Text("Нет свежих данных: $error", color = Orange, fontSize = 12.sp)
    }
}

@Composable
private fun HumanSummary(data: MarketPulseResponse) {
    val accent = regimeColor(data.market_regime.regime)
    PulseCard(accent) {
        Text(data.human_summary.title, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        BadgeRows(
            listOf(
                regimeLabel(data.market_regime.regime),
                permission("RISK", data.market_regime.risk_mode),
                permission("LONG", data.market_regime.long_permission),
                permission("SHORT", data.market_regime.short_permission)
            )
        )
        Spacer(Modifier.height(7.dp))
        Text(data.human_summary.main_text.ifBlank { "Рыночное объяснение пока недоступно." }, color = TxtSoft, fontSize = 13.sp)
    }
}

@Composable
private fun Overview(data: MarketPulseResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverviewCard(
                "Сервер", data.health.status.uppercase(),
                listOf("${data.health.mode} · Up ${data.health.uptime}", "M ${age(data.health.market_age_sec)} · TA ${age(data.health.ta_age_sec)}"),
                Modifier.weight(1f), if (data.health.fresh) Green else Orange
            )
            OverviewCard(
                "Портфель", money(data.portfolio.total_equity),
                listOf("FUT ${money(data.portfolio.fut_equity)} · SPOT ${money(data.portfolio.spot_equity)}", "Позиции ${data.positions.fut.size + data.positions.spot.size}"),
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val fut = data.watchlist.futures
            OverviewCard(
                "Фьючерсы", "${fut.len} идей",
                listOf("Trigger ${fut.trigger_crossed} · Ready ${fut.would_confirm_now}", "SHORT ${fut.sides["SHORT"] ?: 0} · LONG ${fut.sides["LONG"] ?: 0}"),
                Modifier.weight(1f)
            )
            OverviewCard(
                "Pump Advisor", "${data.pump_advisor.symbols_count} монет",
                listOf("Активные ${activePump(data)}", dominantPhase(data)),
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OverviewCard(title: String, value: String, lines: List<String>, modifier: Modifier, valueColor: Color = Txt) {
    Card(modifier.border(1.dp, Divider, RoundedCornerShape(7.dp)), colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(7.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(title.uppercase(), color = TxtSoft, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            lines.forEach { Text(it, color = TxtSoft, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun Indexes(data: MarketPulseResponse) {
    val fusion = data.context_fusion
    val ichi = fusion.ichimoku
    val regime = data.market_regime
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IndexCard("Heatmap", regimeColor(fusion.heatmap_bias), listOf(
            "Bias" to code(fusion.heatmap_bias),
            "Net score" to decimal(fusion.heatmap_net_bias_score),
            "Below EMA20" to mapPct(regime.heatmap, "below_ema20_pct"),
            "RSI bearish" to mapPct(regime.heatmap, "rsi_bearish_pct")
        ), "Общий перевес покупателей или продавцов. Это фон, а не разрешение на вход.")
        IndexCard("Ichimoku", Blue, listOf(
            "Выше облака" to ichi.above_cloud.toString(),
            "Ниже облака" to ichi.below_cloud.toString(),
            "Внутри облака" to ichi.inside_cloud.toString(),
            "LONG / SHORT поддержка" to "${ichi.long_supportive} / ${ichi.short_supportive}"
        ), "Облако помогает оценить ширину тренда. Смешанная структура требует строгого отбора.")
        IndexCard("Futures Pressure", Yellow, listOf(
            "Pressure" to mapText(regime.futures_pressure, "pressure", "bias"),
            "24h positive" to mapPct(regime.futures_pressure, "positive_24h_pct"),
            "Funding positive" to mapPct(regime.futures_pressure, "funding_positive_pct")
        ), "Активность futures сама по себе ещё не является сигналом входа.")
        IndexCard("Vortex Pressure", Green, listOf(
            "Ready allowed" to mapNum(regime.vortex_pressure, "ready_allowed", "ready_allowed_count"),
            "Raw ready no EA" to mapNum(regime.vortex_pressure, "raw_ready_no_ea", "raw_ready_no_ea_count"),
            "Watch only" to mapNum(regime.vortex_pressure, "watch_only", "watch_only_count"),
            "Strategy blocked" to mapNum(regime.vortex_pressure, "strategy_blocked", "strategy_blocked_count")
        ), "Идеи проходят через подтверждение и защитные фильтры до реального входа.")
    }
}

@Composable
private fun IndexCard(title: String, accent: Color, rows: List<Pair<String, String>>, description: String) {
    PulseCard(accent) {
        Text(title, color = Txt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        rows.forEach { Metric(it.first, it.second) }
        Spacer(Modifier.height(4.dp))
        Text(description, color = TxtSoft, fontSize = 11.sp)
    }
}

@Composable
private fun WhyNoTrade(data: MarketPulseResponse) {
    PulseCard(Blue) {
        Text("Почему бот не входит?", color = Txt, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(data.human_summary.why_no_trade.ifBlank { "Причина пока не рассчитана." }, color = TxtSoft, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Metric("Новые входы", if (data.risk.block_new_entries) "Заблокированы" else "Разрешены", if (data.risk.block_new_entries) Red else Green)
        Metric("Дневной PnL", moneySigned(data.risk.daily_realized_pnl), pnlColor(data.risk.daily_realized_pnl))
        Metric("Лимит убытка", moneySigned(data.risk.daily_loss_limit_usdt))
        if (data.risk.block_reason.isNotBlank()) Metric("Risk reason", data.risk.block_reason, Orange)
        if (data.human_summary.what_to_watch.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text("Смотреть: ${data.human_summary.what_to_watch.joinToString(" · ")}", color = Blue, fontSize = 12.sp)
        }
        if (data.human_summary.recommendation.isNotBlank()) {
            Spacer(Modifier.height(5.dp))
            Text(data.human_summary.recommendation, color = Yellow, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WatchSummary(summary: MarketPulseWatchSummary, market: String) {
    PulseCard {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SmallStat("Кандидаты", summary.len.toString(), Modifier.weight(1f))
            SmallStat("Trigger", summary.trigger_crossed.toString(), Modifier.weight(1f))
            SmallStat("Ready", summary.would_confirm_now.toString(), Modifier.weight(1f), if (summary.would_confirm_now > 0) Green else TxtSoft)
            SmallStat("Invalid", summary.invalidated.toString(), Modifier.weight(1f), if (summary.invalidated > 0) Orange else TxtSoft)
        }
        Spacer(Modifier.height(6.dp))
        Metric("Watch / Blocked", "${summary.status_counts["watch"] ?: 0} / ${summary.status_counts["blocked"] ?: 0}")
        Metric("LONG / SHORT", "${summary.sides["LONG"] ?: summary.sides["BUY"] ?: 0} / ${summary.sides["SHORT"] ?: 0}")
        if (summary.confirm_reasons.isNotEmpty()) Metric("Главная причина", dominant(summary.confirm_reasons))
        else if (market == "SPOT") Text("Spot-кандидаты появятся при готовых planner-сигналах.", color = TxtSoft, fontSize = 11.sp)
    }
}

@Composable
private fun NearEntryRow(item: MarketPulseNearEntry) {
    PulseCard(distanceColor(item.dist_pct)) {
        Row {
            Column(Modifier.weight(1f)) {
                Text(item.symbol, color = Txt, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(item.setup_type.ifBlank { "-" }, color = TxtSoft, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.side.ifBlank { "-" }, color = if (item.side == "SHORT") Orange else Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${decimal(item.dist_pct, 4)}%", color = distanceColor(item.dist_pct), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Metric("Raw Sc", item.score.toString())
        Metric("Статус", reasonRu(item.reason.ifBlank { item.stage }), if (item.would_confirm_now) Green else TxtSoft)
    }
}

@Composable
private fun PumpSummary(data: MarketPulseResponse) {
    PulseCard(Orange) {
        Text("Pump Advisor · READ-ONLY", color = Txt, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        if (!data.pump_advisor.available) Text("Snapshot пока недоступен.", color = TxtSoft, fontSize = 12.sp)
        else {
            Metric("Сканируется", "${data.pump_advisor.symbols_count} монет")
            Metric("Активные сценарии", activePump(data).toString())
            Metric("Главная фаза", dominantPhase(data))
            Text("Наблюдает сценарии отдельно и не открывает сделки автоматически.", color = TxtSoft, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PumpRow(item: MarketPulsePumpItem) {
    PulseCard {
        Row {
            Text(item.symbol, modifier = Modifier.weight(1f), color = Txt, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(item.phase.ifBlank { "-" }, color = Orange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            "Score ${item.score} · RSI ${dash(item.rsi14)} · 24h ${percent(item.pump_pct_24h)} · 6h ${percent(item.pump_pct_6h)} · Vol ${dash(item.volume_ratio)}x",
            color = TxtSoft, fontSize = 11.sp
        )
    }
}

@Composable
private fun SmallStat(label: String, value: String, modifier: Modifier, valueColor: Color = Txt) {
    Column(modifier) {
        Text(label, color = TxtSoft, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Metric(label: String, value: String, valueColor: Color = Txt) {
    Row(Modifier.fillMaxWidth().padding(top = 5.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = TxtSoft, fontSize = 11.sp)
        Spacer(Modifier.width(8.dp))
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BadgeRows(labels: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        labels.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { row.forEach { Badge(it) } }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Box(Modifier.background(PanelSoft, RoundedCornerShape(50)).border(1.dp, Divider, RoundedCornerShape(50)).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(text, color = TxtSoft, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun PulseCard(accent: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, accent ?: Divider, RoundedCornerShape(7.dp)),
        colors = CardDefaults.cardColors(containerColor = accent?.copy(alpha = 0.05f) ?: Panel),
        shape = RoundedCornerShape(7.dp)
    ) { Column(Modifier.padding(11.dp), content = content) }
}

@Composable private fun Heading(text: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.width(7.dp).height(7.dp).background(Blue, CircleShape)); Spacer(Modifier.width(7.dp))
    Text(text, color = Txt, fontWeight = FontWeight.Bold, fontSize = 16.sp)
}
@Composable private fun Subheading(text: String) = Text(text, color = Txt, fontWeight = FontWeight.Bold, fontSize = 15.sp)
@Composable private fun EmptyCard(text: String) = PulseCard { Text(text, color = TxtSoft, fontSize = 12.sp) }

private fun activePump(data: MarketPulseResponse): Int = with(data.pump_advisor.phase_counts) { (get("PUMP_DETECTED") ?: 0) + (get("SHORT_CANDIDATE") ?: 0) + (get("BREAKDOWN_WATCH") ?: 0) }
private fun dominantPhase(data: MarketPulseResponse) = data.pump_advisor.phase_counts.maxByOrNull { it.value }?.key ?: "-"
private fun dominant(values: Map<String, Int>) = values.maxByOrNull { it.value }?.let { "${reasonRu(it.key)} · ${it.value}" } ?: "-"
private fun reasonRu(value: String) = when (value) { "waiting_trigger" -> "Ждёт trigger"; "waiting_buffer" -> "Ждёт buffer"; "would_confirm_now" -> "Готово к проверке"; "invalidated" -> "Идея сломана"; "blocked" -> "Заблокировано"; else -> value.ifBlank { "-" } }
private fun regimeLabel(value: String?) = when (value) { "risk_on_bullish" -> "RISK-ON"; "mild_risk_on" -> "УМЕРЕННЫЙ RISK-ON"; "mixed_neutral" -> "СМЕШАННЫЙ РЫНОК"; "mild_risk_off" -> "УМЕРЕННЫЙ RISK-OFF"; "risk_off_bearish" -> "RISK-OFF"; else -> code(value) }
private fun permission(prefix: String, value: String?) = "$prefix ${code(value)}"
private fun code(value: String?) = value?.replace('_', ' ')?.uppercase(Locale.US) ?: "-"
private fun regimeColor(value: String?) = when { value == null -> TxtSoft; value.contains("risk_on") || value.contains("bull") -> Green; value.contains("risk_off") || value.contains("bear") -> Orange; else -> Blue }
private fun distanceColor(value: Double) = when { abs(value) <= .1 -> Red; abs(value) <= .5 -> Orange; abs(value) <= 1 -> Yellow; else -> TxtSoft }
private fun pnlColor(value: Double) = when { value > 0 -> Green; value < 0 -> Red; else -> TxtSoft }
private fun money(value: Double) = String.format(Locale.US, "%.2f", value)
private fun moneySigned(value: Double) = String.format(Locale.US, "%+.2f", value)
private fun decimal(value: Double, digits: Int = 2) = String.format(Locale.US, "%.${digits}f", value)
private fun dash(value: Double?) = value?.let { decimal(it) } ?: "-"
private fun percent(value: Double?) = value?.let { "${decimal(it)}%" } ?: "-"
private fun age(value: Double) = if (value >= 60) "${decimal(value / 60)}м" else "${decimal(value)}с"
private fun mapRaw(values: Map<String, Any?>, vararg keys: String) = keys.firstNotNullOfOrNull { (values[it] as? Number)?.toDouble() }
private fun mapNum(values: Map<String, Any?>, vararg keys: String) = mapRaw(values, *keys)?.let { decimal(it, 0) } ?: "-"
private fun mapPct(values: Map<String, Any?>, vararg keys: String) = mapRaw(values, *keys)?.let { "${decimal(it)}%" } ?: "-"
private fun mapText(values: Map<String, Any?>, vararg keys: String) = keys.firstNotNullOfOrNull { values[it]?.toString() } ?: "-"
