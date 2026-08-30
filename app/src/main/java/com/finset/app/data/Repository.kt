package com.finset.app.data

import kotlinx.coroutines.flow.Flow

class FinSetRepository(private val db: AppDatabase) {

    // 카테고리
    fun observeCategories(): Flow<List<CategoryEntity>> = db.categoryDao().observeAll()
    fun observeInterestedCategories(): Flow<List<CategoryEntity>> = db.categoryDao().observeInterested()
    suspend fun setCategoryInterested(category: CategoryEntity, interested: Boolean) {
        db.categoryDao().update(category.copy(isInterested = interested))
    }

    // 종목
    fun observeStocks(): Flow<List<StockEntity>> = db.stockDao().observeAll()
    fun observeInterestedStocks(): Flow<List<StockEntity>> = db.stockDao().observeInterested()
    suspend fun setStockInterested(stock: StockEntity, interested: Boolean) {
        db.stockDao().update(stock.copy(isInterested = interested))
    }
    suspend fun getStock(ticker: String): StockEntity? = db.stockDao().getByTicker(ticker)

    // 뉴스
    fun observeFeaturedNews(): Flow<List<NewsEntity>> = db.newsDao().observeFeatured()
    fun observeMyNews(): Flow<List<NewsEntity>> = db.newsDao().observeMyNews()
    fun observeAllNews(): Flow<List<NewsEntity>> = db.newsDao().observeAllNews()
    suspend fun getNews(id: Long): NewsEntity? = db.newsDao().getById(id)
    suspend fun getUnmatchedNews(): List<NewsEntity> = db.newsDao().getUnmatchedOnce()
    suspend fun markNewsMatched(id: Long) = db.newsDao().markMatched(id)

    // 옵션 지표
    fun observeOptionMetrics(ticker: String): Flow<OptionMetricsEntity?> = db.optionMetricsDao().observeByTicker(ticker)
    suspend fun getOptionMetricsOnce(ticker: String): OptionMetricsEntity? = db.optionMetricsDao().getOnce(ticker)
    suspend fun updateOptionMetrics(metrics: OptionMetricsEntity) = db.optionMetricsDao().update(metrics)
    suspend fun setAlertEnabled(metrics: OptionMetricsEntity, enabled: Boolean) {
        db.optionMetricsDao().update(metrics.copy(alertEnabled = enabled))
    }

    // 알림함
    fun observeAlerts(): Flow<List<AlertEntity>> = db.alertDao().observeAll()
    fun observeAlertsByType(type: String): Flow<List<AlertEntity>> = db.alertDao().observeByType(type)
    suspend fun insertAlert(alert: AlertEntity): Long = db.alertDao().insert(alert)
}
