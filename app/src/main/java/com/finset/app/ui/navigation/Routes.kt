package com.finset.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val STOCK_LIST = "stockList"
    const val STOCK_DETAIL = "stockDetail/{ticker}"
    const val STOCK_SEARCH = "stockSearch"
    const val ALERTS = "alerts"
    const val MY_PAGE = "myPage"
    const val NEWS_DETAIL = "newsDetail/{newsId}"

    fun stockDetail(ticker: String) = "stockDetail/$ticker"
    fun newsDetail(newsId: Long) = "newsDetail/$newsId"

    /** 하단 네비게이션에 표시할 탭들 */
    val bottomNavRoutes = setOf(HOME, STOCK_LIST, ALERTS, MY_PAGE)
}
