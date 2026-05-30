package com.mikhail.vortex.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikhail.vortex.model.SpotIdea
import com.mikhail.vortex.viewmodel.PlannerViewModel

private val Panel = Color(0xFF171B22)
private val Divider = Color(0xFF2A313D)
private val Txt = Color(0xFFF2F4F8)
private val TxtSoft = Color(0xFFAFB8C5)

private val Blue = Color(0xFF5DA9FF)
private val Green = Color(0xFF31C26A)
private val Red = Color(0xFFFF5E57)
private val Yellow = Color(0xFFFFC245)
private val Neutral = Color(0xFF7F8A99)

private val PlannerGold = Color(0xFFE4C26A)
private val PlannerMint = Color(0xFF59D4B3)
private val PlannerPink = Color(0xFFFF6E8A)

private val PlannerCardBg = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF121722),
        Color(0xFF0F1420)
    )
)

@Composable
fun PlannerTabContent(
    serverStatus: String,
    serverDot: String
) {
    val vm: PlannerViewModel = viewModel()
    val ideas by vm.ideas.collectAsState()
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        vm.startAutoRefresh()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PlannerHeader(serverStatus = serverStatus, serverDot = serverDot)
        }

        item {
            PlannerTopInfo()
        }

        if (ideas.isEmpty()) {
            item {
                EmptyPlannerCard(
                    "Planner пока пуст. Это обычно значит, что сервер только стартовал или идеи ещё пересчитываются."
                )
            }
        } else {
            // Planner Compact Sort v1
            val sortedIdeas = ideas.sortedWith(PlannerSignals.comparator())

            item {
                Text(
                    text = "Отсортировано по близости к сигналу: готовность → зона → confidence → риск",
                    color = TxtSoft,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }

            items(sortedIdeas) { idea ->
                val expanded = expandedMap[idea.symbol] == true
                SpotPlannerCompactCard(
                    idea = idea,
                    expanded = expanded,
                    onToggle = { expandedMap[idea.symbol] = !expanded }
                )
            }
        }
    }
}

@Composable
private fun PlannerHeader(
    serverStatus: String,
    serverDot: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SPOT PLANNER",
                color = Txt,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            when (serverDot) {
                                "green" -> Green
                                "yellow" -> Yellow
                                "red" -> Red
                                else -> Neutral
                            },
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "STATUS: $serverStatus | MODE: PLANNER",
                    color = TxtSoft,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Аналитический режим для спота. Бот не покупает, а считает и показывает сильные сценарии.",
                color = TxtSoft,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PlannerTopInfo() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Как читать карточку",
                color = Txt,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Confidence — качество идеи, Ready — готовность входа, RR — отношение потенциальной прибыли к риску.",
                color = TxtSoft,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyPlannerCard(text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(text = text, color = TxtSoft)
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpotPlannerCompactCard(idea: SpotIdea, expanded: Boolean, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(18.dp))
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier
                .background(PlannerCardBg)
                .padding(14.dp)
        ) {
            PlannerCompactHeader(idea, expanded)

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Divider)
                Spacer(modifier = Modifier.height(12.dp))

                ActionBlock(actionLabel = idea.action_label, actionHint = idea.action_hint)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniMetricCard("Confidence", "${idea.confidence_score} · ${idea.confidence_band}", PlannerGold, Modifier.weight(1f))
                    MiniMetricCard("RR", formatRR(idea.rr_ratio), PlannerMint, Modifier.weight(1f))
                    MiniMetricCard("Риск", shortRiskLabel(idea.risk_grade), riskColor(idea.risk_grade), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoTitle("Текущая цена")
                        InfoValue(formatPrice(idea.current_price))
                        Spacer(modifier = Modifier.height(12.dp))
                        ZoneBlock("${formatPrice(idea.accumulation_zone.top)} — ${formatPrice(idea.accumulation_zone.bottom)}")
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionSubTitle("Лесенка входа")
                        Spacer(modifier = Modifier.height(8.dp))
                        idea.entries.forEach { entry ->
                            LadderRow("${entry.allocation_pct}%", formatPrice(entry.price))
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        SectionSubTitle("Цели TP")
                        Spacer(modifier = Modifier.height(8.dp))
                        idea.targets.forEachIndexed { index, target ->
                            TargetRow("TP${index + 1} ${formatPrice(target.price)}", "${target.close_pct}%")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        InvalidationBlock("ниже ${formatPrice(idea.invalidation)}")
                        Spacer(modifier = Modifier.height(10.dp))
                        MiniMetricCard("Средний вход", formatPrice(idea.avg_entry), Blue, Modifier.fillMaxWidth())
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Divider)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Аргументы", color = PlannerGold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    idea.thesis.forEach { thesis -> ThesisTag(thesis) }
                }
            }
        }
    }
}

@Composable
private fun PlannerCompactHeader(idea: SpotIdea, expanded: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Blue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◈", color = Blue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "#${idea.priority_rank} ${idea.symbol}",
                    color = Txt,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlannerChip(idea.tier, Blue)
                PlannerChip(compactStatusLabel(idea.status), statusColor(idea.status))
                PlannerChip(readinessLabel(idea.readiness), readinessColor(idea.readiness))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Горизонт: ${idea.horizon}", color = TxtSoft, fontSize = 12.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(shortRiskLabel(idea.risk_grade), color = riskColor(idea.risk_grade), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(signalDistanceLabel(idea), color = signalDistanceColor(idea), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(if (expanded) "▲" else "▼", color = TxtSoft, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpotPlannerPremiumCard(idea: SpotIdea) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .background(PlannerCardBg)
                .padding(16.dp)
        ) {
            TopSymbolRow(idea)

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Divider)
            Spacer(modifier = Modifier.height(14.dp))

            ActionBlock(
                actionLabel = idea.action_label,
                actionHint = idea.action_hint
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniMetricCard(
                    title = "Confidence",
                    value = "${idea.confidence_score} · ${idea.confidence_band}",
                    valueColor = PlannerGold,
                    modifier = Modifier.weight(1f)
                )
                MiniMetricCard(
                    title = "RR",
                    value = formatRR(idea.rr_ratio),
                    valueColor = PlannerMint,
                    modifier = Modifier.weight(1f)
                )
                MiniMetricCard(
                    title = "Риск",
                    value = shortRiskLabel(idea.risk_grade),
                    valueColor = riskColor(idea.risk_grade),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.72f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Контекст",
                        color = TxtSoft,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContextBadge("D1 ${prettyTrend(idea.trend_d1)}", trendColor(idea.trend_d1))
                        ContextBadge("W1 ${prettyTrend(idea.trend_w1)}", trendColor(idea.trend_w1))
                        ContextBadge("4H ${prettyStructure(idea.structure_4h)}", structureColor(idea.structure_4h))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoTitle("Текущая цена")
                    InfoValue(formatPrice(idea.current_price))

                    Spacer(modifier = Modifier.height(12.dp))

                    ZoneBlock(
                        "${formatPrice(idea.accumulation_zone.top)} — ${formatPrice(idea.accumulation_zone.bottom)}"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SectionSubTitle("Лесенка набора")
                    Spacer(modifier = Modifier.height(8.dp))

                    idea.entries.forEach { entry ->
                        LadderRow(
                            alloc = "${entry.allocation_pct}%",
                            price = formatPrice(entry.price)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    SectionSubTitle("План фиксации")
                    Spacer(modifier = Modifier.height(8.dp))

                    idea.targets.forEachIndexed { index, target ->
                        TargetRow(
                            target = "TP${index + 1} ${formatPrice(target.price)}",
                            closePct = "${target.close_pct}%"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    InvalidationBlock("ниже ${formatPrice(idea.invalidation)}")

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MiniMetricCard(
                            title = "Base",
                            value = formatPct(idea.expected_return_base_pct),
                            valueColor = Green,
                            modifier = Modifier.weight(1f)
                        )
                        MiniMetricCard(
                            title = "Bull",
                            value = formatPct(idea.expected_return_bull_pct),
                            valueColor = PlannerMint,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MiniMetricCard(
                        title = "Средний вход",
                        value = formatPrice(idea.avg_entry),
                        valueColor = Blue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Divider)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Аргументы",
                color = PlannerGold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                idea.thesis.forEach { thesis ->
                    ThesisTag(thesis)
                }
            }
        }
    }
}

@Composable
private fun TopSymbolRow(idea: SpotIdea) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Blue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◈",
                        color = Blue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "#${idea.priority_rank} ${idea.symbol}",
                    color = Txt,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlannerChip(idea.tier, Blue)
                PlannerChip(idea.status, statusColor(idea.status))
                PlannerChip(readinessLabel(idea.readiness), readinessColor(idea.readiness))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Горизонт: ${idea.horizon}",
                color = TxtSoft,
                fontSize = 13.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = idea.risk_grade,
                color = riskColor(idea.risk_grade),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = priorityLabel(idea.status, idea.readiness),
                color = priorityColor(idea.status, idea.readiness),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ActionBlock(
    actionLabel: String,
    actionHint: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.78f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Действие",
                color = TxtSoft,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = actionLabel,
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = actionHint,
                color = TxtSoft,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun MiniMetricCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.72f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = TxtSoft,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = value,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ContextBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlannerChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun InfoTitle(text: String) {
    Text(text = text, color = TxtSoft, fontSize = 12.sp)
}

@Composable
private fun InfoValue(text: String) {
    Text(
        text = text,
        color = Txt,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
}

@Composable
private fun ZoneBlock(zone: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PlannerGold.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = "Зона набора",
                color = PlannerGold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = zone,
                color = Txt,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun SectionSubTitle(text: String) {
    Text(
        text = text,
        color = Txt,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
}

@Composable
private fun LadderRow(
    alloc: String,
    price: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$alloc позиции",
            color = PlannerGold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "по $price",
            color = Txt,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TargetRow(
    target: String,
    closePct: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = target,
            color = Txt,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Text(
            text = closePct,
            color = PlannerMint,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun InvalidationBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PlannerPink.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = "Инвалидация",
                color = PlannerPink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                color = Txt,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ThesisTag(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF1A2130), RoundedCornerShape(10.dp))
            .border(1.dp, Divider, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(
            text = "• $text",
            color = TxtSoft,
            fontSize = 12.sp
        )
    }
}

private fun readinessLabel(readiness: String): String {
    return when (readiness.uppercase()) {
        "HIGH" -> "ГОТОВ"
        "MID" -> "СРЕДНЯЯ ГОТОВНОСТЬ"
        else -> "НИЗКАЯ ГОТОВНОСТЬ"
    }
}

private fun priorityLabel(status: String, readiness: String): String {
    return when {
        status == "В зоне" && readiness.uppercase() == "HIGH" -> "ПРИОРИТЕТ"
        status == "В зоне" -> "ИНТЕРЕСНО"
        status == "Рядом с зоной" -> "НАБЛЮДАТЬ"
        else -> "РАНО"
    }
}

private fun priorityColor(status: String, readiness: String): Color {
    return when {
        status == "В зоне" && readiness.uppercase() == "HIGH" -> Green
        status == "В зоне" -> Yellow
        status == "Рядом с зоной" -> Blue
        else -> Neutral
    }
}

private fun prettyTrend(trend: String): String {
    return when (trend.lowercase()) {
        "uptrend" -> "вверх"
        "downtrend" -> "вниз"
        else -> "боковик"
    }
}

private fun prettyStructure(structure: String): String {
    return when (structure) {
        "HH/HL" -> "лок. рост"
        "LH/LL" -> "лок. спад"
        else -> "range"
    }
}

private fun shortRiskLabel(risk: String): String {
    return when (risk) {
        "Низкий риск" -> "Низкий"
        "Средний риск" -> "Средний"
        else -> "Высокий"
    }
}

private fun statusColor(status: String): Color {
    return when (status) {
        "В зоне" -> Green
        "Рядом с зоной" -> Yellow
        "Глубоко" -> PlannerPink
        else -> Blue
    }
}

private fun readinessColor(readiness: String): Color {
    return when (readiness.uppercase()) {
        "HIGH" -> Green
        "MID" -> Yellow
        else -> Neutral
    }
}

private fun riskColor(risk: String): Color {
    return when (risk) {
        "Низкий риск" -> Green
        "Средний риск" -> Yellow
        else -> PlannerPink
    }
}

private fun trendColor(trend: String): Color {
    return when (trend.lowercase()) {
        "uptrend" -> Green
        "downtrend" -> PlannerPink
        else -> Yellow
    }
}

private fun structureColor(structure: String): Color {
    return when (structure) {
        "HH/HL" -> Green
        "LH/LL" -> PlannerPink
        else -> Yellow
    }
}

private fun formatPrice(price: Double): String {
    return when {
        price >= 1000 -> String.format("%.2f", price)
        price >= 1 -> String.format("%.4f", price)
        price >= 0.01 -> String.format("%.5f", price)
        else -> String.format("%.8f", price)
    }
}

private fun formatPct(v: Double): String {
    return if (v > 0) String.format("+%.2f%%", v) else String.format("%.2f%%", v)
}


private fun plannerSignalComparator(): Comparator<SpotIdea> {
    return PlannerSignals.comparator()
}

private fun readinessRank(readiness: String): Int {
    return PlannerSignals.readinessRank(readiness)
}

private fun zoneDistanceRank(idea: SpotIdea): Int {
    return PlannerSignals.zoneDistanceRank(idea)
}

private fun riskRank(risk: String): Int {
    return PlannerSignals.riskRank(risk)
}

private fun signalDistanceLabel(idea: SpotIdea): String {
    val readiness = readinessRank(idea.readiness)
    val zone = zoneDistanceRank(idea)
    return when {
        readiness == 0 || zone == 0 -> "ГОТОВО"
        readiness == 1 || zone == 1 -> "СКОРО"
        readiness == 2 || zone == 2 -> "РАНО"
        else -> "ПОЗЖЕ"
    }
}

private fun signalDistanceColor(idea: SpotIdea): Color {
    return when (signalDistanceLabel(idea)) {
        "ГОТОВО" -> Green
        "СКОРО" -> Yellow
        "РАНО" -> TxtSoft
        else -> Neutral
    }
}

private fun compactStatusLabel(status: String): String {
    val st = status.lowercase()
    return when {
        "above" in st || "выше" in st -> "Выше зоны"
        "inside" in st || "внут" in st -> "Внутри зоны"
        "below" in st || "ниже" in st -> "Ниже зоны"
        else -> status.ifBlank { "Статус" }
    }
}


private fun formatRR(v: Double): String {
    return String.format("%.2f", v)
}
