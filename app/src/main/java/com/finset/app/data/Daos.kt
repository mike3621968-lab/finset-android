package com.finset.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isInterested = 1")
    fun observeInterested(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun observeAll(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE isInterested = 1")
    fun observeInterested(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE ticker = :ticker LIMIT 1")
    suspend fun getByTicker(ticker: String): StockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockEntity>)

    @Update
    suspend fun update(stock: StockEntity)

    @Query("SELECT COUNT(*) FROM stocks")
    suspend fun count(): Int
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news WHERE isFeatured = 1 ORDER BY id ASC")
    fun observeFeatured(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE isFeatured = 0 AND isMatched = 1 ORDER BY matchedAt DESC")
    fun observeMyNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE isFeatured = 0 ORDER BY id DESC")
    fun observeAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NewsEntity?

    @Query("SELECT * FROM news WHERE isFeatured = 0 AND isMatched = 0")
    suspend fun getUnmatchedOnce(): List<NewsEntity>

    @Query("UPDATE news SET isMatched = 1, matchedAt = :matchedAt WHERE id = :id")
    suspend fun markMatched(id: Long, matchedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsEntity>)

    @Query("SELECT COUNT(*) FROM news")
    suspend fun count(): Int
}

@Dao
interface OptionMetricsDao {
    @Query("SELECT * FROM option_metrics WHERE ticker = :ticker LIMIT 1")
    fun observeByTicker(ticker: String): Flow<OptionMetricsEntity?>

    @Query("SELECT * FROM option_metrics WHERE ticker = :ticker LIMIT 1")
    suspend fun getOnce(ticker: String): OptionMetricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metrics: List<OptionMetricsEntity>)

    @Update
    suspend fun update(metrics: OptionMetricsEntity)

    @Query("SELECT COUNT(*) FROM option_metrics")
    suspend fun count(): Int
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY id DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE type = :type ORDER BY id DESC")
    fun observeByType(type: String): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity): Long

    @Query("SELECT COUNT(*) FROM alerts")
    suspend fun count(): Int
}
