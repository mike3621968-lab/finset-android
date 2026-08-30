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

    private var simulationJob: Job? = null
    private val lastZoneByTicker = mutableMapOf<String, String>()

    fun toggleSimulation() {
        if (_simulationEnabled.value) stopSimulation() else startSimulation()
    }

    private fun startSimulation() {
        if (simulationJob != null) return
        _simulationEnabled.value = true
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(7000)
                runCatching { simulateNewsTick() }
                delay(1500)
                runCatching { simulateTradeTick() }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _simulationEnabled.value = false
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
        val stock = candidates.random()
        val metrics = repo.getOptionMetricsOnce(stock.ticker) ?: return
        if (!metrics.alertEnabled) return

        val oldPct = metrics.currentPercent
        val delta = Random.nextInt(-9, 10)
        val newPct = (oldPct + delta).coerceIn(2, 98)
        repo.updateOptionMetrics(metrics.copy(currentPercent = newPct))

        val oldZone = zoneOf(oldPct, metrics.putWallPercent, metrics.callWallPercent)
        val newZone = zoneOf(newPct, metrics.putWallPercent, metrics.callWallPercent)
        if (newZone == oldZone || newZone == "neutral") return
        if (lastZoneByTicker[stock.ticker] == newZone) return
        lastZoneByTicker[stock.ticker] = newZone

        val (title, subtitle) = when (newZone) {
            "call_near" -> "${stock.ticker} 콜월 근접 알림" to "현재 위치 ${newPct}% · 콜월(${metrics.callWall})까지 근접 - 진입 신호 주의"
            "call_breach" -> "${stock.ticker} 콜월 돌파" to "콜월(${metrics.callWall})을 돌파했습니다 - 청산/익절 검토 구간"
            "put_near" -> "${stock.ticker} 풋월 근접 알림" to "현재 위치 ${newPct}% · 풋월(${metrics.putWall})까지 근접 - 리스크 관리 필요"
            else -> "${stock.ticker} 풋월 이탈" to "풋월(${metrics.putWall})을 이탈했습니다 - 손절/헷지 검토 구간"
        }

        repo.insertAlert(
            AlertEntity(
                type = "trade",
                title = title,
                subtitle = subtitle,
                timeLabel = "방금 전",
                relatedTicker = stock.ticker
            )
        )
        NotificationHelper.notifyTrade(context = getApplication(), ticker = stock.ticker, title = title, body = subtitle)
    }

    private fun zoneOf(pct: Int, putPct: Int, callPct: Int): String = when {
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
