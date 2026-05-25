package com.mikhail.vortex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikhail.vortex.model.ContextFusionSymbol
import com.mikhail.vortex.ui.planner.PlannerTabContent
import com.mikhail.vortex.ui.intelligence.IntelligenceTabContent
import com.mikhail.vortex.viewmodel.DashboardViewModel
import com.mikhail.vortex.viewmodel.LogCardUi
import com.mikhail.vortex.viewmodel.LogFilter
import com.mikhail.vortex.viewmodel.MiniWatchUi
import com.mikhail.vortex.viewmodel.PositionCardUi
import com.mikhail.vortex.viewmodel.SummaryPnlUi
import kotlinx.coroutines.delay

private val Bg = Color(0xFF0E1116)
private val Panel = Color(0xFF171B22)
private val PanelSoft = Color(0xFF1C212A)
private val Divider = Color(0xFF2A313D)
private val Txt = Color(0xFFF2F4F8)
private val TxtSoft = Color(0xFFAFB8C5)
private val Blue = Color(0xFF5DA9FF)
private val Yellow = Color(0xFFFFC245)
private val Green = Color(0xFF31C26A)
private val Red = Color(0xFFFF5E57)
private val Orange = Color(0xFFFF9F43)
private val Purple = Color(0xFFB978FF)
private val Neutral = Color(0xFF7F8A99)
private val FutBadge = Color(0xFF3D6DFF)
private val SpotBadge = Color(0xFFFFB020)
private val FutOpenTint = Color(0x143D6DFF)
private val SpotOpenTint = Color(0x14FFB020)
private val PosGreenBg = Color(0x1625A55B)
private val PosRedBg = Color(0x16D94B4B)
private val PosNeutralBg = Color(0x101C212A)
private val LogBlueBg = Color(0x143D6DFF)
private val LogYellowBg = Color(0x14FFC245)
private val LogRedBg = Color(0x16D94B4B)
private val LogGreenBg = Color(0x1625A55B)

enum class BottomTab {
    TERMINAL,
    LOGS,
    PLANNER,
    INTELLIGENCE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VortexTerminalScreen()
        }
    }
}

@Composable
fun VortexTerminalScreen() {
    val vm: DashboardViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(BottomTab.TERMINAL) }

    val spotFree by vm.spotFree.collectAsState()
    val spotEquity by vm.spotEquity.collectAsState()
    val futuresFree by vm.futuresFree.collectAsState()
    val futuresEquity by vm.futuresEquity.collectAsState()
    val totalEquity by vm.totalEquity.collectAsState()

    val serverStatus by vm.serverStatus.collectAsState()
    val serverLine by vm.serverLine.collectAsState()
    val serverDot by vm.serverDot.collectAsState()
    val mode by vm.mode.collectAsState()
    val futuresOpenCount by vm.futuresOpenCount.collectAsState()
    val spotOpenCount by vm.spotOpenCount.collectAsState()
    val futuresPositions by vm.futuresPositions.collectAsState()
    val spotPositions by vm.spotPositions.collectAsState()
    val miniWatchlist by vm.miniWatchlist.collectAsState()
    val summary by vm.summary.collectAsState()
    val logCards by vm.logCards.collectAsState()
    val logFilter by vm.logFilter.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            vm.loadData()
            delay(3000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Bg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                BottomTab.TERMINAL -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            HeaderBlock(
                                status = serverStatus,
                                serverLine = serverLine,
                                serverDot = serverDot,
                                mode = mode
                            )
                        }

                        item {
                            EquityBlock(
                                spotFree = spotFree,
                                spotEquity = spotEquity,
                                futuresFree = futuresFree,
                                futuresEquity = futuresEquity,
                                totalEquity = totalEquity,
                                futuresOpenCount = futuresOpenCount,
                                spotOpenCount = spotOpenCount
                            )
                        }

                        item {
                            TodayPnlBlock(summary = summary)
                        }

                        item {
                            SectionTitle("🔵 FUTURES POSITIONS")
                        }

                        if (futuresPositions.isEmpty()) {
                            item { EmptySectionCard("Нет активных фьючерсных позиций") }
                        } else {
                            items(futuresPositions) { pos -> PositionCard(pos) }
                        }

                        item {
                            SectionTitle("🟡 SPOT POSITIONS")
                        }

                        if (spotPositions.isEmpty()) {
                            item { EmptySectionCard("Нет активных спот-позиций") }
                        } else {
                            items(spotPositions) { pos -> PositionCard(pos) }
                        }

                        item {
                            SectionTitle("👀 WATCHLIST MINI")
                        }

                        if (miniWatchlist.isEmpty()) {
                            item { EmptySectionCard("Нет кандидатов") }
                        } else {
                            item { MiniWatchlistBlock(miniWatchlist) }
                        }
                    }
                }

                BottomTab.LOGS -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            HeaderBlock(
                                status = serverStatus,
                                serverLine = serverLine,
                                serverDot = serverDot,
                                mode = mode
                            )
                        }

                        item {
                            SectionTitle("🧾 SERVER / TRADE LOGS")
                        }

                        item {
                            LogFilterRow(
                                selected = logFilter,
                                onSelect = { vm.setLogFilter(it) }
                            )
                        }

                        if (logCards.isEmpty()) {
                            item { EmptySectionCard("Нет событий") }
                        } else {
                            items(logCards) { log -> LogCard(log) }
                        }
                    }
                }

                BottomTab.PLANNER -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        PlannerTabContent(
                            serverStatus = serverStatus,
                            serverDot = serverDot
                        )
                    }
                }

                BottomTab.INTELLIGENCE -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        IntelligenceTabContent(
                            serverStatus = serverStatus,
                            serverDot = serverDot
                        )
                    }
                }
            }

            BottomBar(
                selectedTab = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    }
}

@Composable
fun LogFilterRow(
    selected: LogFilter,
    onSelect: (LogFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip("ALL", selected == LogFilter.ALL) { onSelect(LogFilter.ALL) }
        FilterChip("TRADES", selected == LogFilter.TRADES) { onSelect(LogFilter.TRADES) }
        FilterChip("SYSTEM", selected == LogFilter.SYSTEM) { onSelect(LogFilter.SYSTEM) }
        FilterChip("RISK", selected == LogFilter.RISK) { onSelect(LogFilter.RISK) }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Blue.copy(alpha = 0.18f) else PanelSoft
    val fg = if (selected) Blue else TxtSoft

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, Divider, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BottomBar(
    selectedTab: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BottomTabButton(
                title = "Terminal",
                selected = selectedTab == BottomTab.TERMINAL,
                modifier = Modifier.weight(1f)
            ) {
                onSelect(BottomTab.TERMINAL)
            }

            BottomTabButton(
                title = "Logs",
                selected = selectedTab == BottomTab.LOGS,
                modifier = Modifier.weight(1f)
            ) {
                onSelect(BottomTab.LOGS)
            }

            BottomTabButton(
                title = "Planner",
                selected = selectedTab == BottomTab.PLANNER,
                modifier = Modifier.weight(1f)
            ) {
                onSelect(BottomTab.PLANNER)
            }

            BottomTabButton(
                title = "Intel",
                selected = selectedTab == BottomTab.INTELLIGENCE,
                modifier = Modifier.weight(1f)
            ) {
                onSelect(BottomTab.INTELLIGENCE)
            }
        }
    }
}

@Composable
fun BottomTabButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) Blue.copy(alpha = 0.18f) else PanelSoft
    val fg = if (selected) Blue else TxtSoft

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = modifier
            .height(50.dp)
            .border(1.dp, Divider, RoundedCornerShape(14.dp)),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HeaderBlock(
    status: String,
    serverLine: String,
    serverDot: String,
    mode: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "VORTEX TERMINAL",
                color = Txt,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(
                    when (serverDot) {
                        "green" -> Green
                        "yellow" -> Yellow
                        "red" -> Red
                        else -> Neutral
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STATUS: $status | MODE: $mode",
                    color = when (status) {
                        "ONLINE" -> Green
                        "DELAY" -> Yellow
                        "OFFLINE" -> Red
                        else -> TxtSoft
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = serverLine,
                color = TxtSoft,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EquityBlock(
    spotFree: String,
    spotEquity: String,
    futuresFree: String,
    futuresEquity: String,
    totalEquity: String,
    futuresOpenCount: Int,
    spotOpenCount: Int
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PORTFOLIO",
                color = TxtSoft,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "SPOT FREE",
                    value = spotFree,
                    sub = "Equity: $spotEquity | Open: $spotOpenCount",
                    accent = SpotBadge,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "FUTURES FREE",
                    value = futuresFree,
                    sub = "Equity: $futuresEquity | Open: $futuresOpenCount",
                    accent = FutBadge,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PanelSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TOTAL EQUITY",
                        color = TxtSoft,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = totalEquity,
                        color = Txt,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TodayPnlBlock(summary: SummaryPnlUi) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TODAY PNL",
                color = TxtSoft,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PnlMiniCard("FUT Realized", summary.futRealized, summary.futRealizedColor, Modifier.weight(1f))
                PnlMiniCard("SPOT Realized", summary.spotRealized, summary.spotRealizedColor, Modifier.weight(1f))
                PnlMiniCard("Total Realized", summary.totalRealized, summary.totalRealizedColor, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PnlMiniCard("FUT Open", summary.futOpen, summary.futOpenColor, Modifier.weight(1f))
                PnlMiniCard("SPOT Open", summary.spotOpen, summary.spotOpenColor, Modifier.weight(1f))
                PnlMiniCard("Total Open", summary.totalOpen, summary.totalOpenColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = title,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = Txt,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = sub,
                color = TxtSoft,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PnlMiniCard(
    title: String,
    value: String,
    colorName: String,
    modifier: Modifier = Modifier
) {
    val color = when (colorName) {
        "green" -> Green
        "red" -> Red
        else -> TxtSoft
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PanelSoft),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = TxtSoft,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TxtSoft,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
}

@Composable
fun EmptySectionCard(text: String) {
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

@Composable
fun PositionCard(pos: PositionCardUi) {
    val pnlColor = when (pos.pnlColor) {
        "green" -> Green
        "red" -> Red
        else -> TxtSoft
    }

    val bgColor = when (pos.pnlColor) {
        "green" -> PosGreenBg
        "red" -> PosRedBg
        else -> PosNeutralBg
    }

    val badgeColor = if (pos.marketType == "FUTURES") FutBadge else SpotBadge
    val badgeBg = if (pos.marketType == "FUTURES") FutOpenTint else SpotOpenTint

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pos.symbol,
                        color = Txt,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(pos.sideText, badgeColor.copy(alpha = 0.18f), badgeColor)
                        Badge(pos.marketType, badgeBg, badgeColor)
                        Badge(pos.statusLabel, PanelSoft, TxtSoft)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = pos.pnlText,
                        color = pnlColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pos.setupType.uppercase(),
                        color = Blue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Divider)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricBlock("Entry", pos.entryText, Modifier.weight(1f))
                MetricBlock(
                    if (pos.marketType == "SPOT") "Avg" else "Mark",
                    if (pos.marketType == "SPOT") pos.avgText else pos.markText,
                    Modifier.weight(1f)
                )
                MetricBlock(
                    if (pos.marketType == "FUTURES") "Lev" else "State",
                    if (pos.marketType == "FUTURES") pos.leverageText else pos.statusLabel,
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricBlock("SL", pos.slText, Modifier.weight(1f))
                MetricBlock("TP1", pos.tp1Text, Modifier.weight(1f))
                MetricBlock("TP2", pos.tp2Text, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = pos.extraText,
                color = TxtSoft,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = TxtSoft, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Txt,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MiniWatchlistBlock(items: List<MiniWatchUi>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            items.forEachIndexed { idx, item ->
                if (idx > 0) {
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.symbol,
                                color = Txt,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (item.hasFutures) {
                                MiniMarketBadge("F", FutBadge)
                            }

                            if (item.hasSpot) {
                                Spacer(modifier = Modifier.width(5.dp))
                                MiniMarketBadge("S", SpotBadge)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusBadge(
                                text = item.statusText,
                                color = watchStatusColor(item.statusColor)
                            )

                            Text(
                                text = item.setupType,
                                color = TxtSoft,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.reasonText,
                            color = TxtSoft,
                            fontSize = 12.sp
                        )

                        if (item.triggerText != "-" || item.invalidationText != "-") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Trigger: ${item.triggerText} | Invalid: ${item.invalidationText}",
                                color = Neutral,
                                fontSize = 11.sp
                            )
                        }

                        if (item.fusionAvailable) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FusionMiniBlock(fusion = item.fusion)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.scoreText,
                            color = TxtSoft,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.fusionScoreText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.fusionScoreText,
                                color = fusionColor(item.fusion?.final?.view),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.priceText,
                            color = TxtSoft,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.expiresText,
                            color = Neutral,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FusionMiniBlock(fusion: ContextFusionSymbol?) {
    if (fusion == null) {
        Box(
            modifier = Modifier
                .background(PanelSoft, RoundedCornerShape(10.dp))
                .border(1.dp, Divider, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Fusion: no data",
                color = Neutral,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        return
    }

    val view = fusion.final?.view
    val color = fusionColor(view)
    val zone = compactZoneName(fusion.setup_zone?.preferred_zone)
    val zoneQuality = fusion.setup_zone?.zone_quality?.let { " q$it" }.orEmpty()
    val heat = compactHeatmapName(
        fusion.heatmap?.global?.bias ?: fusion.heatmap?.local_bias
    )
    val blocker = fusion.final?.blockers?.firstOrNull()
        ?: fusion.policy?.code
        ?: fusion.strategy?.blocked_reason
    val need = fusionNeedText(fusion)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.34f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = fusionLabel(view),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Zone: $zone$zoneQuality | Heat: $heat",
            color = TxtSoft,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = if (blocker.isNullOrBlank()) need else "Block: $blocker",
            color = if (blocker.isNullOrBlank()) TxtSoft else color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

fun watchStatusColor(name: String): Color {
    return when (name) {
        "green" -> Green
        "yellow" -> Yellow
        "red" -> Red
        "blue" -> Blue
        else -> Neutral
    }
}

@Composable
fun MiniMarketBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LogCard(log: LogCardUi) {
    val accent = when (log.colorName) {
        "green" -> Green
        "red" -> Red
        "yellow" -> Yellow
        "blue" -> Blue
        else -> Neutral
    }

    val bg = when (log.colorName) {
        "green" -> LogGreenBg
        "red" -> LogRedBg
        "yellow" -> LogYellowBg
        "blue" -> LogBlueBg
        else -> Panel
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Badge(log.badgeText, accent.copy(alpha = 0.16f), accent)
                Text(
                    text = log.timeText,
                    color = TxtSoft,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = log.title,
                color = Txt,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (log.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.subtitle,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.body,
                color = TxtSoft,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun Badge(
    text: String,
    bg: Color,
    fg: Color
) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(9.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, CircleShape)
    )
}

fun fusionLabel(view: String?): String {
    return when (view) {
        "ENTRY_CANDIDATE_STRONG" -> "✅ STRONG"
        "RAW_CANDIDATE_WAIT_EA_GOOD_ZONE" -> "🧠 WAIT EA / GOOD ZONE"
        "RAW_CANDIDATE_WAIT_EA" -> "WAIT EA"
        "RAW_CANDIDATE_BAD_CONTEXT" -> "BAD CONTEXT"
        "POLICY_BLOCKED" -> "🛡 POLICY BLOCKED"
        "WATCH_GOOD_ZONE_WAIT_TRIGGER" -> "GOOD ZONE / WAIT TRIGGER"
        "WATCH_ONLY" -> "WATCH ONLY"
        "STRATEGY_BLOCKED" -> "STRATEGY BLOCKED"
        "NO_TA_DATA" -> "NO TA"
        else -> "Fusion: ${view?.replace('_', ' ') ?: "no view"}"
    }
}

fun fusionColor(view: String?): Color {
    return when (view) {
        "ENTRY_CANDIDATE_STRONG" -> Green
        "RAW_CANDIDATE_WAIT_EA_GOOD_ZONE" -> Blue
        "RAW_CANDIDATE_WAIT_EA" -> Color(0xFF7895B2)
        "RAW_CANDIDATE_BAD_CONTEXT" -> Orange
        "POLICY_BLOCKED" -> Red
        "WATCH_GOOD_ZONE_WAIT_TRIGGER" -> Purple
        "WATCH_ONLY" -> Neutral
        "STRATEGY_BLOCKED" -> Neutral
        "NO_TA_DATA" -> Color(0xFF4F5865)
        else -> TxtSoft
    }
}

fun compactZoneName(zone: String?): String {
    return when (zone) {
        "long_pullback_zone" -> "Long Pullback"
        "short_pullback_zone" -> "Short Pullback"
        "middle_no_trade_zone" -> "Mid / No Trade"
        "high_zone" -> "High Zone"
        "low_zone" -> "Low Zone"
        "neutral_zone" -> "Neutral"
        null, "" -> "-"
        else -> zone.replace('_', ' ')
    }
}

fun compactHeatmapName(bias: String?): String {
    return when (bias) {
        "strong_bullish" -> "Strong Bull"
        "mild_bullish" -> "Mild Bull"
        "mixed_neutral" -> "Mixed"
        "mild_bearish" -> "Mild Bear"
        "strong_bearish" -> "Strong Bear"
        "no_data" -> "No Data"
        null, "" -> "-"
        else -> bias.replace('_', ' ')
    }
}

fun fusionNeedText(fusion: ContextFusionSymbol): String {
    val warnings = fusion.final?.warnings.orEmpty()
    val reasons = fusion.final?.reasons.orEmpty()
    val eaLabel = fusion.ea?.label ?: fusion.ea?.grade
    return when {
        warnings.isNotEmpty() -> "Need: ${warnings.first()}"
        reasons.isNotEmpty() -> reasons.first()
        eaLabel.isNullOrBlank() -> "Need: trigger/EA"
        else -> "EA: $eaLabel"
    }
}
