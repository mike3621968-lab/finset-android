package com.finset.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val label: String,
    val isInterested: Boolean = false
)

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val ticker: String,
    val name: String,
    val type: String,          // "index" | "etf" | "stock"
    val iconBg: String,        // hex color
    val iconColor: String,     // hex color
    val price: String,
    val changePercent: String, // e.g. "+0.42%"
    val isPositive: Boolean = true,
    val isInterested: Boolean = false
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String,
    val title: String,
    val source: String,
    val timeLabel: String,
    val body: String,
    val tickers: String,       // comma separated tickers
    val isFeatured: Boolean = false,  // 오늘의 주요뉴스 카드용
    val isMatched: Boolean = false,   // 내 관심에 매칭되는지
    val matchedAt: Long = 0L          // 실제로 매칭된 시각 (최신순 정렬용, epoch millis)
)

@Entity(tableName = "option_metrics")
data class OptionMetricsEntity(
    @PrimaryKey val ticker: String,
    val gammaExposure: String,   // e.g. "+4.2B"
    val deltaExposure: String,   // e.g. "-1.8B"
    val zeroGamma: String,       // e.g. "$182.50"
    val volatilityTrigger: String,
    val putWall: String,
    val callWall: String,
    val putWallPercent: Int,     // 0~100, 트랙 상 위치
    val currentPercent: Int,
    val callWallPercent: Int,
    val zeroGammaPercent: Int,   // 0~100, 제로감마 트랙 상 위치 (진입 시그널 판정용)
    val volatilityTriggerPercent: Int, // 0~100, VT 트랙 상 위치 (VT 근접/이탈 판정용)
    val expertNote: String,
    val updatedAt: String,
    val alertEnabled: Boolean = true
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,          // "news" | "trade"
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val relatedTicker: String? = null,
    val relatedNewsId: Long? = null
)
