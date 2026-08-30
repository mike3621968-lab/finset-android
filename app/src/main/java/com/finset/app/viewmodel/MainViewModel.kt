package com.finset.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.finset.app.data.AppDatabase
import com.finset.app.data.CategoryEntity
import com.finset.app.data.FinSetRepository
import com.finset.app.data.OptionMetricsEntity
import com.finset.app.data.StockEntity
import com.finset.app.data.seedDatabaseIfEmpty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}
