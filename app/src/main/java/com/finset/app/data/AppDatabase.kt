package com.finset.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        StockEntity::class,
        NewsEntity::class,
        OptionMetricsEntity::class,
        AlertEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun stockDao(): StockDao
    abstract fun newsDao(): NewsDao
    abstract fun optionMetricsDao(): OptionMetricsDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finset.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * 서버 없이, 앱 최초 실행 시 SeedData(하드코딩)를 로컬 DB에 한 번만 채워 넣는다.
 * 이후 사용/삭제/추가는 전부 로컬 DB에서만 이루어진다.
 */
suspend fun seedDatabaseIfEmpty(db: AppDatabase) {
    if (db.categoryDao().count() == 0) {
        db.categoryDao().insertAll(SeedData.categories)
    }
    if (db.stockDao().count() == 0) {
        db.stockDao().insertAll(SeedData.stocks)
    }
    if (db.newsDao().count() == 0) {
        db.newsDao().insertAll(SeedData.news)
    }
    if (db.optionMetricsDao().count() == 0) {
        db.optionMetricsDao().insertAll(SeedData.optionMetrics)
    }
    if (db.alertDao().count() == 0) {
        db.alertDao().insertAll(SeedData.alerts)
    }
}

fun seedDatabaseAsync(db: AppDatabase, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
    scope.launch { seedDatabaseIfEmpty(db) }
}
