package com.mikhail.vortex.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhail.vortex.model.ContextFusionBlock
import com.mikhail.vortex.model.ContextFusionSymbol
import com.mikhail.vortex.model.DashboardResponse
import com.mikhail.vortex.model.Position
import com.mikhail.vortex.model.TerminalCard
import com.mikhail.vortex.model.TodaySummary
import com.mikhail.vortex.model.WatchlistItem
import com.mikhail.vortex.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.mikhail.vortex.model.WatchlistResponse

data class PositionCardUi(
    val symbol: String,
    val marketType: String,
    val sideText: String,
    val setupType: String,
    val statusLabel: String,
    val entryText: String,
    val avgText: String,
    val markText: String,
    val pnlText: String,
    val pnlColor: String,
    val slText: String,
    val tp1Text: String,
    val tp2Text: String,
    val leverageText: String,
    val extraText: String
)

data class SummaryPnlUi(
    val futRealized: String = "0.00",
    val spotRealized: String = "0.00",
    val totalRealized: String = "0.00",
    val futOpen: String = "0.00",
    val spotOpen: String = "0.00",
    val totalOpen: String = "0.00",
    val futRealizedColor: String = "neutral",
    val spotRealizedColor: String = "neutral",
    val totalRealizedColor: String = "neutral",
    val futOpenColor: String = "neutral",
    val spotOpenColor: String = "neutral",
    val totalOpenColor: String = "neutral"
)

data class MiniWatchUi(
    val symbol: String,
    val setupType: String,
    val scoreText: String,
    val fusionScoreText: String = "",
    val priceText: String,
    val hasFutures: Boolean = false,
    val hasSpot: Boolean = false,
    val status: String = "watch",
    val statusText: String = "WATCH",
    val statusColor: String = "blue",
    val reasonText: String = "",
    val triggerText: String = "-",
    val invalidationText: String = "-",
    val expiresText: String = "-",
    val fusionAvailable: Boolean = false,
    val fusion: ContextFusionSymbol? = null
)

data class LogCardUi(
    val category: String,
    val badgeText: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val timeText: String,
    val colorName: String
)

enum class LogFilter {
    ALL, TRADES, SYSTEM, RISK
}

class DashboardViewModel : ViewModel() {

    private val _spotFree = MutableStateFlow("0.00")
    val spotFree: StateFlow<String> = _spotFree

    private val _spotEquity = MutableStateFlow("0.00")
    val spotEquity: StateFlow<String> = _spotEquity

    private val _futuresFree = MutableStateFlow("0.00")
    val futuresFree: StateFlow<String> = _futuresFree

    private val _futuresEquity = MutableStateFlow("0.00")
    val futuresEquity: StateFlow<String> = _futuresEquity

    private val _totalEquity = MutableStateFlow("0.00")
    val totalEquity: StateFlow<String> = _totalEquity

    private val _serverStatus = MutableStateFlow("INIT")
    val serverStatus: StateFlow<String> = _serverStatus

    private val _serverLine = MutableStateFlow("Server: проверка...")
    val serverLine: StateFlow<String> = _serverLine

    private val _serverDot = MutableStateFlow("gray")
    val serverDot: StateFlow<String> = _serverDot

    private val _mode = MutableStateFlow("PAPER")
    val mode: StateFlow<String> = _mode

    private val _futuresOpenCount = MutableStateFlow(0)
    val futuresOpenCount: StateFlow<Int> = _futuresOpenCount

    private val _spotOpenCount = MutableStateFlow(0)
    val spotOpenCount: StateFlow<Int> = _spotOpenCount

    private val _futuresPositions = MutableStateFlow<List<PositionCardUi>>(emptyList())
    val futuresPositions: StateFlow<List<PositionCardUi>> = _futuresPositions

    private val _spotPositions = MutableStateFlow<List<PositionCardUi>>(emptyList())
    val spotPositions: StateFlow<List<PositionCardUi>> = _spotPositions

    private val _miniWatchlist = MutableStateFlow<List<MiniWatchUi>>(emptyList())
    val miniWatchlist: StateFlow<List<MiniWatchUi>> = _miniWatchlist

    private val _topFusionCandidates = MutableStateFlow<List<ContextFusionSymbol>>(emptyList())
    val topFusionCandidates: StateFlow<List<ContextFusionSymbol>> = _topFusionCandidates

    private val _fusionCandidatesAvailable = MutableStateFlow(false)
    val fusionCandidatesAvailable: StateFlow<Boolean> = _fusionCandidatesAvailable

    private val _fusionCandidatesSourceCount = MutableStateFlow(0)
    val fusionCandidatesSourceCount: StateFlow<Int> = _fusionCandidatesSourceCount

    private val _summary = MutableStateFlow(SummaryPnlUi())
    val summary: StateFlow<SummaryPnlUi> = _summary

    private val _allLogCards = MutableStateFlow<List<LogCardUi>>(emptyList())
    private val _logCards = MutableStateFlow<List<LogCardUi>>(emptyList())
    val logCards: StateFlow<List<LogCardUi>> = _logCards

    private val _logFilter = MutableStateFlow(LogFilter.ALL)
    val logFilter: StateFlow<LogFilter> = _logFilter

    fun setLogFilter(filter: LogFilter) {
        _logFilter.value = filter
        applyLogFilter()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val dashboard = ApiClient.api.getDashboard()
                val health = ApiClient.api.getHealth()

                Log.d(
                    "VORTEX_DEBUG",
                    "todayOpenFut=${dashboard.today.today_open_fut}, " +
                            "todayOpenSpot=${dashboard.today.today_open_spot}, " +
                            "futPos=${dashboard.positions.fut.keys}, " +
                            "spotPos=${dashboard.positions.spot.keys}, " +
                            "marketKeys=${dashboard.market.prices.keys}"
                )

                _spotFree.value = formatMoney(dashboard.portfolio.spot_free)
                _spotEquity.value = formatMoney(dashboard.portfolio.spot_equity)
                _futuresFree.value = formatMoney(dashboard.portfolio.fut_free)
                _futuresEquity.value = formatMoney(dashboard.portfolio.fut_equity)
                _totalEquity.value = formatMoney(dashboard.portfolio.total_equity)

                _mode.value = health.mode

                _serverStatus.value = when (health.status.lowercase()) {
                    "online" -> "ONLINE"
                    "degraded" -> "DELAY"
                    else -> "OFFLINE"
                }

                _serverDot.value = when (health.status.lowercase()) {
                    "online" -> "green"
                    "degraded" -> "yellow"
                    else -> "red"
                }

                _serverLine.value =
                    "Server: ${health.status.uppercase()} | ${health.mode} | ping ${health.ping_ms} ms | market ${health.market_age_sec}s | ta ${health.ta_age_sec}s"

                _futuresOpenCount.value = dashboard.positions.fut.size
                _spotOpenCount.value = dashboard.positions.spot.size

                val futCardMap = dashboard.terminal.futures_cards.associateBy { it.symbol }
                val spotCardMap = dashboard.terminal.spot_cards.associateBy { it.symbol }

                _futuresPositions.value = buildPositionListFromState(
                    dashboard = dashboard,
                    positions = dashboard.positions.fut,
                    marketType = "FUTURES",
                    cardMap = futCardMap
                )

                _spotPositions.value = buildPositionListFromState(
                    dashboard = dashboard,
                    positions = dashboard.positions.spot,
                    marketType = "SPOT",
                    cardMap = spotCardMap
                )

                val watchlistResponse: WatchlistResponse? = try {
                    ApiClient.api.getWatchlist()
                } catch (e: Exception) {
                    null
                }

                _fusionCandidatesAvailable.value = dashboard.context_fusion.available
                _fusionCandidatesSourceCount.value = fusionCandidateSourceCount(dashboard.context_fusion)
                _topFusionCandidates.value = buildTopFusionCandidates(dashboard.context_fusion)

                _miniWatchlist.value = if (watchlistResponse != null) {
                    buildMiniWatchlistFromApi(
                        items = watchlistResponse.data.all,
                        fusionBlock = dashboard.context_fusion
                    )
                } else {
                    buildMiniWatchlist(
                        dashboard.terminal.futures_cards,
                        dashboard.terminal.spot_cards,
                        dashboard.context_fusion
                    )
                }

                _summary.value = buildSummary(dashboard.today)

                _allLogCards.value = buildLogCards(dashboard.system.sys_logs)
                applyLogFilter()

            } catch (e: Exception) {
                _serverStatus.value = "OFFLINE"
                _serverDot.value = "red"
                _serverLine.value = "Server: OFFLINE | ${e.message ?: "ошибка связи"}"

                _spotFree.value = "0.00"
                _spotEquity.value = "0.00"
                _futuresFree.value = "0.00"
                _futuresEquity.value = "0.00"
                _totalEquity.value = "0.00"

                _futuresOpenCount.value = 0
                _spotOpenCount.value = 0
                _futuresPositions.value = emptyList()
                _spotPositions.value = emptyList()
                _miniWatchlist.value = emptyList()
                _topFusionCandidates.value = emptyList()
                _fusionCandidatesAvailable.value = false
                _fusionCandidatesSourceCount.value = 0
                _summary.value = SummaryPnlUi()
                _allLogCards.value = emptyList()
                _logCards.value = emptyList()
            }
        }
    }

    private fun applyLogFilter() {
        _logCards.value = when (_logFilter.value) {
            LogFilter.ALL -> _allLogCards.value
            LogFilter.TRADES -> _allLogCards.value.filter { it.category == "TRADE" }
            LogFilter.SYSTEM -> _allLogCards.value.filter { it.category == "SYSTEM" }
            LogFilter.RISK -> _allLogCards.value.filter { it.category == "RISK" }
        }
    }

    private fun buildPositionListFromState(
        dashboard: DashboardResponse,
        positions: Map<String, Position>,
        marketType: String,
        cardMap: Map<String, TerminalCard>
    ): List<PositionCardUi> {
        return positions.entries.map { (symbol, position) ->
            val card = cardMap[symbol]
            val marketPrice = resolveMarketPrice(dashboard, symbol, card)

            buildPositionCardUi(
                symbol = symbol,
                position = position,
                marketType = marketType,
                marketPrice = marketPrice,
                setupType = card?.setup_type ?: "-"
            )
        }
    }

    private fun resolveMarketPrice(
        dashboard: DashboardResponse,
        symbol: String,
        card: TerminalCard?
    ): Double {
        val cardPrice = card?.price ?: 0.0
        if (cardPrice > 0.0) return cardPrice

        val marketPrice = dashboard.market.prices[symbol] ?: 0.0
        if (marketPrice > 0.0) return marketPrice

        val taPrice = dashboard.market.ta_data[symbol]?.price ?: 0.0
        if (taPrice > 0.0) return taPrice

        return 0.0
    }

    private fun buildSummary(today: TodaySummary): SummaryPnlUi {
        return SummaryPnlUi(
            futRealized = formatSignedMoney(today.today_realized_fut),
            spotRealized = formatSignedMoney(today.today_realized_spot),
            totalRealized = formatSignedMoney(today.today_total_realized),
            futOpen = formatSignedMoney(today.today_open_fut),
            spotOpen = formatSignedMoney(today.today_open_spot),
            totalOpen = formatSignedMoney(today.today_total_open),
            futRealizedColor = pnlColorName(today.today_realized_fut),
            spotRealizedColor = pnlColorName(today.today_realized_spot),
            totalRealizedColor = pnlColorName(today.today_total_realized),
            futOpenColor = pnlColorName(today.today_open_fut),
            spotOpenColor = pnlColorName(today.today_open_spot),
            totalOpenColor = pnlColorName(today.today_total_open)
        )
    }

    private fun buildMiniWatchlistFromApi(
        items: List<WatchlistItem>,
        fusionBlock: ContextFusionBlock
    ): List<MiniWatchUi> {
        val fusionBySymbol = fusionBySymbol(fusionBlock)

        return items
            .sortedWith(
                compareBy<WatchlistItem> { statusRank(it.status) }
                    .thenByDescending { it.score }
                    .thenBy { it.symbol }
            )
            .take(12)
            .map { item ->
                val fusion = fusionBySymbol[item.symbol.uppercase()]
                MiniWatchUi(
                    symbol = item.symbol.uppercase(),
                    setupType = normalizeSetup(item.setupType),
                    scoreText = "Raw Sc ${item.score}",
                    fusionScoreText = fusion?.final?.score?.let { "Fusion $it" }.orEmpty(),
                    priceText = formatPrice(item.price),
                    hasFutures = item.market.lowercase() == "fut",
                    hasSpot = item.market.lowercase() == "spot",
                    status = item.status.lowercase(),
                    statusText = statusText(item),
                    statusColor = statusColorName(item.status),
                    reasonText = reasonText(item),
                    triggerText = if (item.triggerPrice > 0.0) formatPrice(item.triggerPrice) else "-",
                    invalidationText = if (item.invalidationPrice > 0.0) formatPrice(item.invalidationPrice) else "-",
                    expiresText = expiresText(item.expiresInSec),
                    fusionAvailable = fusionBlock.available,
                    fusion = fusion
                )
            }
    }

    private fun buildMiniWatchlist(
        futuresCards: List<TerminalCard>,
        spotCards: List<TerminalCard>,
        fusionBlock: ContextFusionBlock
    ): List<MiniWatchUi> {
        data class WatchAgg(
            val symbol: String,
            var setupType: String = "-",
            var score: Int = 0,
            var price: Double = 0.0,
            var hasFutures: Boolean = false,
            var hasSpot: Boolean = false,
            var status: String = "watch",
            var reason: String = ""
        )

        val map = linkedMapOf<String, WatchAgg>()

        futuresCards.forEach { card ->
            val current = map.getOrPut(card.symbol) {
                WatchAgg(symbol = card.symbol)
            }
            current.hasFutures = true
            if ((card.score ?: 0) >= current.score) {
                current.score = card.score ?: 0
                current.setupType = card.setup_type ?: "-"
                current.price = card.price
                current.status = card.status
                current.reason = card.args_text
            }
        }

        spotCards.forEach { card ->
            val current = map.getOrPut(card.symbol) {
                WatchAgg(symbol = card.symbol)
            }
            current.hasSpot = true
            if ((card.score ?: 0) >= current.score) {
                current.score = card.score ?: 0
                current.setupType = card.setup_type ?: "-"
                current.price = card.price
                current.status = card.status
                current.reason = card.args_text
            }
        }

        val fusionBySymbol = fusionBySymbol(fusionBlock)

        return map.values
            .sortedWith(
                compareBy<WatchAgg> { statusRank(it.status) }
                    .thenByDescending { it.score }
                    .thenBy { it.symbol }
            )
            .take(12)
            .map {
                val fusion = fusionBySymbol[it.symbol.uppercase()]
                MiniWatchUi(
                    symbol = it.symbol,
                    setupType = normalizeSetup(it.setupType),
                    scoreText = "Raw Sc ${it.score}",
                    fusionScoreText = fusion?.final?.score?.let { score -> "Fusion $score" }.orEmpty(),
                    priceText = formatPrice(it.price),
                    hasFutures = it.hasFutures,
                    hasSpot = it.hasSpot,
                    status = it.status.lowercase(),
                    statusText = statusText(it.status, it.reason),
                    statusColor = statusColorName(it.status),
                    reasonText = it.reason.ifBlank { "-" },
                    triggerText = "-",
                    invalidationText = "-",
                    expiresText = "-",
                    fusionAvailable = fusionBlock.available,
                    fusion = fusion
                )
            }
    }

    private fun fusionBySymbol(fusionBlock: ContextFusionBlock): Map<String, ContextFusionSymbol> {
        if (!fusionBlock.available) return emptyMap()
        return fusionBlock.symbols
            .filter { it.symbol.isNotBlank() }
            .associateBy { it.symbol.uppercase() }
    }

    private fun buildTopFusionCandidates(
        fusionBlock: ContextFusionBlock
    ): List<ContextFusionSymbol> {
        if (!fusionBlock.available) return emptyList()
        val source = fusionBlock.important.ifEmpty { fusionBlock.symbols }
        return topFusionCandidates(source)
    }

    private fun fusionCandidateSourceCount(fusionBlock: ContextFusionBlock): Int {
        if (!fusionBlock.available) return 0
        return fusionBlock.important.ifEmpty { fusionBlock.symbols }.size
    }

    private fun statusRank(status: String): Int {
        return when (status.lowercase()) {
            "confirmed" -> 0
            "watch" -> 1
            "recovery_watch" -> 2
            "blocked" -> 3
            else -> 4
        }
    }

    private fun statusText(item: WatchlistItem): String {
        return statusText(item.status, item.waitingFor.ifBlank { item.argsText })
    }

    private fun statusText(status: String, reason: String): String {
        val normalized = status.lowercase()
        val r = reason.lowercase()

        return when {
            normalized == "confirmed" -> "🔥 READY"
            normalized == "watch" -> "👀 WATCH"
            normalized == "recovery_watch" -> "🟡 RECOVERY"
            normalized == "blocked" && r.contains("dead") -> "❌ DEAD"
            normalized == "blocked" && r.contains("chaotic") -> "⚠️ CHAOTIC"
            normalized == "blocked" -> "⛔ BLOCKED"
            else -> normalized.uppercase().ifBlank { "-" }
        }
    }

    private fun statusColorName(status: String): String {
        return when (status.lowercase()) {
            "confirmed" -> "green"
            "watch" -> "blue"
            "recovery_watch" -> "yellow"
            "blocked" -> "red"
            else -> "neutral"
        }
    }

    private fun reasonText(item: WatchlistItem): String {
        return item.waitingFor
            .ifBlank { item.confirmationReason }
            .ifBlank { item.argsText }
            .ifBlank { "-" }
    }

    private fun normalizeSetup(setup: String?): String {
        val value = setup?.trim().orEmpty()
        return if (value.isBlank() || value == "null") "-" else value
    }

    private fun expiresText(seconds: Int): String {
        if (seconds <= 0) return "-"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    private fun buildPositionCardUi(
        symbol: String,
        position: Position,
        marketType: String,
        marketPrice: Double,
        setupType: String
    ): PositionCardUi {
        val pnlNet = position.pnl_net ?: 0.0
        val sideText = if (marketType == "FUTURES") (position.side ?: "-") else "BUY"
        val statusLabel = position.status_label ?: "OPEN"

        val avgText = if (marketType == "SPOT") formatNullablePrice(position.avg_price) else "-"
        val tp1Text = if (marketType == "FUTURES") formatNullablePrice(position.tp1) else formatNullablePrice(position.tp)
        val tp2Text = if (marketType == "FUTURES") formatNullablePrice(position.tp2) else "-"
        val leverageText = if (marketType == "FUTURES" && position.leverage != null) {
            "x${trimZeros(position.leverage)}"
        } else {
            "-"
        }

        val extraText = if (marketType == "FUTURES") {
            "Liq: ${formatNullablePrice(position.liq_price)}"
        } else {
            "Fills: ${position.fills_count ?: 1}"
        }

        return PositionCardUi(
            symbol = symbol,
            marketType = marketType,
            sideText = sideText,
            setupType = setupType,
            statusLabel = statusLabel,
            entryText = formatNullablePrice(position.entry),
            avgText = avgText,
            markText = formatNullablePrice(marketPrice),
            pnlText = formatSignedMoney(pnlNet),
            pnlColor = pnlColorName(pnlNet),
            slText = formatNullablePrice(position.sl),
            tp1Text = tp1Text,
            tp2Text = tp2Text,
            leverageText = leverageText,
            extraText = extraText
        )
    }

    private fun buildLogCards(rawLogs: List<String>): List<LogCardUi> {
        return rawLogs.map { parseLogCard(it) }
    }

    private fun parseLogCard(raw: String): LogCardUi {
        val cleaned = raw.trim()
        val timeRegex = Regex("""(\d{2}:\d{2}:\d{2})""")
        val timeText = timeRegex.find(cleaned)?.value ?: "--:--:--"

        val tagRegex = Regex("""\[(.*?)\]""")
        val tags = tagRegex.findAll(cleaned).map { it.groupValues[1] }.toList()

        val category = when {
            cleaned.contains("[FUT OPEN]") || cleaned.contains("[SPOT OPEN]") ||
                    cleaned.contains("[FUT CLOSE]") || cleaned.contains("[SPOT CLOSE]") -> "TRADE"
            cleaned.contains("[RISK]") -> "RISK"
            else -> "SYSTEM"
        }

        val badgeText = when {
            cleaned.contains("[FUT OPEN]") -> "FUT OPEN"
            cleaned.contains("[SPOT OPEN]") -> "SPOT OPEN"
            cleaned.contains("[FUT CLOSE]") -> "FUT CLOSE"
            cleaned.contains("[SPOT CLOSE]") -> "SPOT CLOSE"
            cleaned.contains("[POOL]") -> "POOL"
            cleaned.contains("[SCREENER]") -> "SCREENER"
            cleaned.contains("[ORACLE]") -> "ORACLE"
            cleaned.contains("[RISK]") -> "RISK"
            else -> tags.lastOrNull() ?: "LOG"
        }

        val content = cleaned
            .replace(Regex("""🕒\s*\d{2}:\d{2}:\d{2}\s*"""), "")
            .replace(Regex("""[^\u0000-\u007F\u0400-\u04FF]+"""), " ")
            .replace(Regex("""\[(.*?)\]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        return when (category) {
            "TRADE" -> parseTradeLog(content, badgeText, timeText)
            "RISK" -> LogCardUi(
                category = category,
                badgeText = badgeText,
                title = "Risk event",
                subtitle = "",
                body = content,
                timeText = timeText,
                colorName = "yellow"
            )
            else -> LogCardUi(
                category = category,
                badgeText = badgeText,
                title = content.take(42).ifBlank { "System event" },
                subtitle = "",
                body = content,
                timeText = timeText,
                colorName = systemColorName(badgeText)
            )
        }
    }

    private fun parseTradeLog(content: String, badgeText: String, timeText: String): LogCardUi {
        val parts = content.split("|").map { it.trim() }
        val head = parts.getOrNull(0).orEmpty()
        val details = parts.drop(1).joinToString("\n")

        val words = head.split(" ")
        val symbol = words.getOrNull(0) ?: "UNKNOWN"
        val sideOrReason = words.getOrNull(1) ?: ""
        val setupMaybe = words.getOrNull(2) ?: ""

        val title = when {
            badgeText.contains("OPEN") -> "$symbol $sideOrReason"
            badgeText.contains("CLOSE") -> "$symbol $sideOrReason"
            else -> symbol
        }

        val subtitle = when {
            badgeText.contains("OPEN") -> "Setup: $setupMaybe"
            badgeText.contains("CLOSE") -> "Reason: $sideOrReason"
            else -> ""
        }

        val colorName = when {
            badgeText.contains("OPEN") -> "blue"
            details.contains("PnL: -") || content.contains("SL") || content.contains("HARD_SL") -> "red"
            details.contains("PnL:") || content.contains("STALL") || content.contains("TIMEOUT") || content.contains("FADE") || content.contains("BU") -> "green"
            else -> "neutral"
        }

        return LogCardUi(
            category = "TRADE",
            badgeText = badgeText,
            title = title,
            subtitle = subtitle,
            body = if (details.isBlank()) content else details,
            timeText = timeText,
            colorName = colorName
        )
    }

    private fun systemColorName(badge: String): String {
        return when (badge) {
            "POOL" -> "blue"
            "SCREENER" -> "yellow"
            "ORACLE" -> "neutral"
            else -> "neutral"
        }
    }

    private fun formatMoney(v: Double): String = String.format("%.2f", v)

    private fun formatSignedMoney(v: Double): String {
        return if (v > 0) String.format("+%.2f", v) else String.format("%.2f", v)
    }

    private fun formatPrice(price: Double): String {
        return when {
            price >= 1000 -> String.format("%.2f", price)
            price >= 1 -> String.format("%.4f", price)
            price >= 0.01 -> String.format("%.5f", price)
            else -> String.format("%.8f", price)
        }
    }

    private fun formatNullablePrice(price: Double?): String {
        if (price == null) return "-"
        return formatPrice(price)
    }

    private fun pnlColorName(v: Double): String {
        return when {
            v > 0 -> "green"
            v < 0 -> "red"
            else -> "neutral"
        }
    }

    private fun trimZeros(v: Double): String {
        return if (v % 1.0 == 0.0) {
            String.format("%.0f", v)
        } else {
            String.format("%.2f", v).trimEnd('0').trimEnd('.')
        }
    }
}

fun topFusionCandidates(symbols: List<ContextFusionSymbol>): List<ContextFusionSymbol> {
    return symbols
        .filter { symbol ->
            val view = symbol.final?.view
            val score = symbol.final?.score ?: 0
            when (view) {
                "ENTRY_CANDIDATE_STRONG",
                "RAW_CANDIDATE_WAIT_EA_GOOD_ZONE",
                "POLICY_BLOCKED",
                "RAW_CANDIDATE_WAIT_EA",
                "WATCH_GOOD_ZONE_WAIT_TRIGGER",
                "ENTRY_CANDIDATE_WEAK_CONTEXT" -> true
                "RAW_CANDIDATE_BAD_CONTEXT" -> score >= 50
                else -> false
            }
        }
        .sortedWith(
            compareBy<ContextFusionSymbol> { fusionPriority(it.final?.view) }
                .thenByDescending { it.final?.score ?: 0 }
                .thenBy { it.symbol.uppercase() }
        )
        .take(8)
}

private fun fusionPriority(view: String?): Int {
    return when (view) {
        "ENTRY_CANDIDATE_STRONG" -> 0
        "RAW_CANDIDATE_WAIT_EA_GOOD_ZONE" -> 1
        "POLICY_BLOCKED" -> 2
        "RAW_CANDIDATE_WAIT_EA" -> 3
        "WATCH_GOOD_ZONE_WAIT_TRIGGER" -> 4
        "ENTRY_CANDIDATE_WEAK_CONTEXT" -> 5
        "RAW_CANDIDATE_BAD_CONTEXT" -> 6
        else -> 99
    }
}
