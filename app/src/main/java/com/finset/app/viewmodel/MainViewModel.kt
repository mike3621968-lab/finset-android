package com.finset.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.finset.app.data.AlertEntity
import com.finset.app.data.AppDatabase
import com.finset.app.data.CategoryEntity
import com.finset.app.data.FinSetRepository
import com.finset.app.data.OptionMetricsEntity
import com.finset.app.data.StockEntity
import com.finset.app.data.seedDatabaseIfEmpty
import com.finset.app.kis.KisPriceRepository
import com.finset.app.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = FinSetRepository(db)
    private val kisRepo = KisPriceRepository(application)

    init {
        // 서버 없이: 최초 실행 시 하드코딩 데이터를 로컬 DB에 1회 삽입
        viewModelScope.launch {
            seedDatabaseIfEmpty(db)
        }
    }

    val categories: StateFlow<List<CategoryEntity>> =
        repo.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stocks: StateFlow<List<StockEntity>> =
        repo.observeStocks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interestedStocks: StateFlow<List<StockEntity>> =
        repo.observeInterestedStocks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myNews = repo.observeMyNews().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNews = repo.observeAllNews().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val featuredNews = repo.observeFeaturedNews().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val alerts = repo.observeAlerts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCategory(category: CategoryEntity) {
        viewModelScope.launch { repo.setCategoryInterested(category, !category.isInterested) }
    }

    fun toggleStockInterest(stock: StockEntity) {
        viewModelScope.launch { repo.setStockInterested(stock, !stock.isInterested) }
    }

    fun removeStockInterest(stock: StockEntity) {
        viewModelScope.launch { repo.setStockInterested(stock, false) }
    }

    fun optionMetricsFlow(ticker: String) = repo.observeOptionMetrics(ticker)

    fun toggleAlertEnabled(metrics: OptionMetricsEntity) {
        viewModelScope.launch { repo.setAlertEnabled(metrics, !metrics.alertEnabled) }
    }

    suspend fun getNews(id: Long) = repo.getNews(id)
    suspend fun getStock(ticker: String) = repo.getStock(ticker)

    // ── 알림 시연용 시뮬레이션 엔진 (서버 없이 앱 안에서 흉내) ──
    private val _simulationEnabled = MutableStateFlow(false)
    val simulationEnabled: StateFlow<Boolean> = _simulationEnabled.asStateFlow()

    private var newsJob: Job? = null
    private var tradeJob: Job? = null
    private val lastWallZone = mutableMapOf<String, String>()
    private val lastVtState = mutableMapOf<String, String>()
    private val regimeByTicker = mutableMapOf<String, String>() // "bull" | "bear"

    // 방향성 시그널(진입/청산) 전용 팝업 이벤트 - 앱이 켜져있을 때 눈에 띄는 다이얼로그로 표시
    private val _popupEvent = MutableStateFlow<TradeSignalPopup?>(null)
    val popupEvent: StateFlow<TradeSignalPopup?> = _popupEvent.asStateFlow()

    fun consumePopup() {
        _popupEvent.value = null
    }

    /**
     * 시뮬레이션이 켜져 있을 때, 실제 조건 충족을 기다리지 않고
     * 원하는 시점에 바로 팝업을 띄워보기 위한 테스트용 함수.
     * kind: "bull_entry" | "bear_entry" | "bull_exit" | "bear_exit"
     */
    fun fireTestPopup(kind: String) {
        val pool = interestedStocks.value.ifEmpty { stocks.value }
        val stock = pool.randomOrNull() ?: return
        val (title, message) = when (kind) {
            "bull_entry" -> "상승진입 시그널" to "제로감마 상향 돌파 - 롱 진입 유리한 구간 진입 (테스트 발동)"
            "bear_entry" -> "하락진입 시그널" to "제로감마 하향 이탈 - 숏 진입 유리한 구간 진입 (테스트 발동)"
            "bull_exit" -> "상승청산 시그널" to "콜월 도달 - 보유 롱 포지션 익절/청산 검토 (테스트 발동)"
            else -> "하락청산 시그널" to "풋월 도달 - 보유 숏 포지션 청산(커버) 검토 (테스트 발동)"
        }
        _popupEvent.value = TradeSignalPopup(stock.ticker, stock.name, stock.price, title, message)
    }

    fun toggleSimulation() {
        if (_simulationEnabled.value) stopSimulation() else startSimulation()
    }

    private fun startSimulation() {
        if (newsJob != null || tradeJob != null) return
        _simulationEnabled.value = true

        newsJob = viewModelScope.launch {
            while (true) {
                delay(17000)
                runCatching { simulateNewsTick() }
            }
        }
        tradeJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                runCatching { simulateTradeTick() }
            }
        }
    }

    private fun stopSimulation() {
        newsJob?.cancel()
        newsJob = null
        tradeJob?.cancel()
        tradeJob = null
        _simulationEnabled.value = false
    }

    // ── 한국투자증권 Open API 실시간 시세 연동 ──
    private val _livePriceEnabled = MutableStateFlow(false)
    val livePriceEnabled: StateFlow<Boolean> = _livePriceEnabled.asStateFlow()

    private val _liveConnectionError = MutableStateFlow<String?>(null)
    val liveConnectionError: StateFlow<String?> = _liveConnectionError.asStateFlow()

    private var livePriceJob: Job? = null

    init {
        // 실시간 시세는 기본으로 켜져있는 게 자연스러우니 앱 시작 시 자동으로 시작.
        // (레이트리밋 등 문제 생기면 홈 화면 토글로 언제든 끌 수 있음)
        startLivePrice()
    }

    fun toggleLivePrice() {
        if (_livePriceEnabled.value) stopLivePrice() else startLivePrice()
    }

    private fun startLivePrice() {
        if (livePriceJob != null) return
        _livePriceEnabled.value = true
        _liveConnectionError.value = null
        livePriceJob = viewModelScope.launch {
            while (true) {
                runCatching { pollLivePrices() }
                    .onFailure { _liveConnectionError.value = "시세 조회 실패: ${it.message}" }
                delay(15000)
            }
        }
    }

    private fun stopLivePrice() {
        livePriceJob?.cancel()
        livePriceJob = null
        _livePriceEnabled.value = false
    }

    private suspend fun pollLivePrices() {
        val targets = interestedStocks.value.filter { kisRepo.isSupported(it.ticker) }
        if (targets.isEmpty()) {
            _liveConnectionError.value = "실시간 연동 대상 종목이 없어요 (지수 SPX/NDX/VIX는 미지원)"
            return
        }
        var anySuccess = false
        var lastError: String? = null
        for (stock in targets) {
            runCatching { kisRepo.fetchPriceOrThrow(stock.ticker) }
                .onSuccess { live ->
                    repo.updateStockPrice(stock, live.price, live.changePercent, live.isPositive)
                    anySuccess = true
                }
                .onFailure { e ->
                    lastError = e.message
                }
        }
        _liveConnectionError.value = if (anySuccess) null else (lastError ?: "알 수 없는 오류")
    }

    private suspend fun simulateNewsTick() {
        val interestedLabels = categories.value.filter { it.isInterested }.map { it.label }.toSet()
        if (interestedLabels.isEmpty()) return

        val candidates = repo.getUnmatchedNews().filter { it.tag in interestedLabels }
        val picked = candidates.randomOrNull() ?: return

        repo.markNewsMatched(picked.id)
        repo.insertAlert(
            AlertEntity(
                type = "news",
                title = picked.title,
                subtitle = "관심 카테고리 '${picked.tag}' 관련 새 뉴스가 도착했어요",
                timeLabel = "방금 전",
                relatedNewsId = picked.id
            )
        )
        NotificationHelper.notifyNews(
            context = getApplication(),
            newsId = picked.id,
            title = "[${picked.tag}] ${picked.title}",
            body = "${picked.source} · 관심 카테고리 새 속보"
        )
    }

    private suspend fun simulateTradeTick() {
        val candidates = interestedStocks.value
        if (candidates.isEmpty()) return

        for (stock in candidates) {
            val metrics = repo.getOptionMetricsOnce(stock.ticker) ?: continue
            if (!metrics.alertEnabled) continue

            val oldPct = metrics.currentPercent
            val delta = Random.nextInt(-16, 17)
            val newPct = (oldPct + delta).coerceIn(2, 98)
            if (newPct == oldPct) continue
            repo.updateOptionMetrics(metrics.copy(currentPercent = newPct))

            val ticker = stock.ticker
            val putPct = metrics.putWallPercent
            val callPct = metrics.callWallPercent
            val zgPct = metrics.zeroGammaPercent
            val vtPct = metrics.volatilityTriggerPercent

            // ① 풋월/콜월 근접·돌파
            val oldWallZone = wallZoneOf(oldPct, putPct, callPct)
            val newWallZone = wallZoneOf(newPct, putPct, callPct)
            if (newWallZone != oldWallZone && newWallZone != "neutral" && lastWallZone[ticker] != newWallZone) {
                lastWallZone[ticker] = newWallZone
                val (title, body) = when (newWallZone) {
                    "call_near" -> "$ticker 콜월 근접 알림" to "현재 위치 ${newPct}% · 콜월(${metrics.callWall})까지 근접"
                    "call_breach" -> "$ticker 콜월 돌파" to "콜월(${metrics.callWall})을 돌파했습니다"
                    "put_near" -> "$ticker 풋월 근접 알림" to "현재 위치 ${newPct}% · 풋월(${metrics.putWall})까지 근접"
                    else -> "$ticker 풋월 이탈" to "풋월(${metrics.putWall})을 이탈했습니다"
                }
                fireTradeAlert(ticker, title, body)
            }

            // ② VT(Volatility Trigger) 근접·이탈
            val vtNearBefore = kotlin.math.abs(oldPct - vtPct) <= 5
            val vtNearAfter = kotlin.math.abs(newPct - vtPct) <= 5
            if (vtNearAfter && !vtNearBefore && lastVtState[ticker] != "near") {
                lastVtState[ticker] = "near"
                fireTradeAlert(
                    ticker, "$ticker VT 근접 알림",
                    "현재 위치 ${newPct}% · Volatility Trigger(${metrics.volatilityTrigger}) 근접 - 변동성 확대 가능성 주시"
                )
            }
            val vtSideBefore = oldPct >= vtPct
            val vtSideAfter = newPct >= vtPct
            if (vtSideAfter != vtSideBefore) {
                val stateKey = if (vtSideAfter) "breach_up" else "breach_down"
                if (lastVtState[ticker] != stateKey) {
                    lastVtState[ticker] = stateKey
                    fireTradeAlert(
                        ticker, "$ticker VT 이탈 알림",
                        "Volatility Trigger(${metrics.volatilityTrigger})를 이탈했습니다 - 변동성 레짐 전환 가능성"
                    )
                }
            }

            // ③ 제로감마 통과 → 진입 시그널 (방향에 따라 이후 레짐을 기억해뒀다가 ④ 청산 시그널에 사용)
            val zgSideBefore = oldPct >= zgPct
            val zgSideAfter = newPct >= zgPct
            if (zgSideAfter != zgSideBefore) {
                if (zgSideAfter) {
                    if (regimeByTicker[ticker] != "bull") {
                        regimeByTicker[ticker] = "bull"
                        val msg = "제로감마(${metrics.zeroGamma}) 상향 돌파 - 롱 진입 유리한 구간 진입"
                        fireTradeAlert(ticker, "$ticker 상승진입 시그널", msg)
                        _popupEvent.value = TradeSignalPopup(ticker, stock.name, stock.price, "상승진입 시그널", msg)
                    }
                } else {
                    if (regimeByTicker[ticker] != "bear") {
                        regimeByTicker[ticker] = "bear"
                        val msg = "제로감마(${metrics.zeroGamma}) 하향 이탈 - 숏 진입 유리한 구간 진입"
                        fireTradeAlert(ticker, "$ticker 하락진입 시그널", msg)
                        _popupEvent.value = TradeSignalPopup(ticker, stock.name, stock.price, "하락진입 시그널", msg)
                    }
                }
            }

            // ④ 진입 이후 반대편 월 도달 → 청산 시그널
            if (newWallZone == "call_breach" && oldWallZone != "call_breach" && regimeByTicker[ticker] == "bull") {
                val msg = "콜월(${metrics.callWall}) 도달 - 보유 롱 포지션 익절/청산 검토"
                fireTradeAlert(ticker, "$ticker 상승청산 시그널", msg)
                _popupEvent.value = TradeSignalPopup(ticker, stock.name, stock.price, "상승청산 시그널", msg)
            }
            if (newWallZone == "put_breach" && oldWallZone != "put_breach" && regimeByTicker[ticker] == "bear") {
                val msg = "풋월(${metrics.putWall}) 도달 - 보유 숏 포지션 청산(커버) 검토"
                fireTradeAlert(ticker, "$ticker 하락청산 시그널", msg)
                _popupEvent.value = TradeSignalPopup(ticker, stock.name, stock.price, "하락청산 시그널", msg)
            }
        }
    }

    private suspend fun fireTradeAlert(ticker: String, title: String, subtitle: String) {
        repo.insertAlert(
            AlertEntity(
                type = "trade",
                title = title,
                subtitle = subtitle,
                timeLabel = "방금 전",
                relatedTicker = ticker
            )
        )
        NotificationHelper.notifyTrade(context = getApplication(), ticker = ticker, title = title, body = subtitle)
    }

    private fun wallZoneOf(pct: Int, putPct: Int, callPct: Int): String = when {
        pct <= putPct -> "put_breach"
        pct <= putPct + 6 -> "put_near"
        pct >= callPct -> "call_breach"
        pct >= callPct - 6 -> "call_near"
        else -> "neutral"
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}

/** 방향성 매매 시그널(진입/청산) 발생 시 앱 안에서 보여줄 팝업 정보 */
data class TradeSignalPopup(
    val ticker: String,
    val stockName: String,
    val price: String,
    val signalType: String, // "상승진입 시그널" | "하락진입 시그널" | "상승청산 시그널" | "하락청산 시그널"
    val message: String
)
