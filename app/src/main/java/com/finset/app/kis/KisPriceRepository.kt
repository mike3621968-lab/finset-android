package com.finset.app.kis

import android.content.Context
import com.finset.app.BuildConfig

data class LivePrice(
    val price: String,        // 예: "228.40"
    val changePercent: String, // 예: "+0.36%" 또는 "-0.36%"
    val isPositive: Boolean
)

class KisPriceRepository(context: Context) {

    private val api: KisApi by lazy { KisApiClient.create() }
    private val tokenManager = KisTokenManager(context)

    /**
     * 종목 티커 → (거래소코드) 매핑.
     * NAS=나스닥, NYS=뉴욕(NYSE), AMS=아멕스.
     * 지수(SPX, NDX, VIX)는 이 API로 조회가 안 되는 종목이라 매핑에서 제외됨 - 실시간 갱신 대상 아님.
     */
    private val exchangeByTicker = mapOf(
        "QQQ" to "NAS",
        "SPY" to "AMS",
        "DIA" to "AMS",
        "IWM" to "AMS",
        "AAPL" to "NAS",
        "MSFT" to "NAS",
        "NVDA" to "NAS",
        "TSLA" to "NAS",
        "AMZN" to "NAS",
        "GOOGL" to "NAS",
        "META" to "NAS",
        "NFLX" to "NAS",
        "AMD" to "NAS",
        "AVGO" to "NAS",
        "PLTR" to "NYS",
        "COIN" to "NAS",
        "MU" to "NAS",
    )

    fun isSupported(ticker: String): Boolean = exchangeByTicker.containsKey(ticker)

    suspend fun fetchPrice(ticker: String): LivePrice? {
        val excd = exchangeByTicker[ticker] ?: return null
        val appKey = BuildConfig.KIS_APP_KEY
        val appSecret = BuildConfig.KIS_APP_SECRET
        if (appKey.isBlank() || appSecret.isBlank()) return null

        val token = tokenManager.getValidToken(appKey, appSecret) ?: return null

        return runCatching {
            val res = api.getOverseasPrice(
                authorization = "Bearer $token",
                appKey = appKey,
                appSecret = appSecret,
                excd = excd,
                symb = ticker
            )
            if (!res.isSuccessful) return@runCatching null
            val output = res.body()?.output ?: return@runCatching null
            val last = output.last?.toDoubleOrNull() ?: return@runCatching null
            val rate = output.rate?.toDoubleOrNull() ?: 0.0
            val isPositive = when (output.sign) {
                "1", "2" -> true
                "4", "5" -> false
                else -> rate >= 0.0
            }
            LivePrice(
                price = formatPrice(last),
                changePercent = "${if (isPositive) "+" else ""}${"%.2f".format(rate)}%",
                isPositive = isPositive
            )
        }.getOrNull()
    }

    private fun formatPrice(value: Double): String {
        return if (value >= 1000) "%,.2f".format(value) else "%.2f".format(value)
    }
}
