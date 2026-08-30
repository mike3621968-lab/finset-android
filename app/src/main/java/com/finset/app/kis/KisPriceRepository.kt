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

    /** 실패 시 사유를 담은 예외를 던진다 (성공 시에만 LivePrice 반환) */
    suspend fun fetchPriceOrThrow(ticker: String): LivePrice {
        val excd = exchangeByTicker[ticker]
            ?: throw IllegalArgumentException("미지원 종목: $ticker")
        val appKey = BuildConfig.KIS_APP_KEY
        val appSecret = BuildConfig.KIS_APP_SECRET
        if (appKey.isBlank() || appSecret.isBlank()) {
            throw IllegalStateException("APP KEY/SECRET이 비어있음 (local.properties 또는 GitHub Secrets 확인 필요)")
        }

        val token = tokenManager.getValidToken(appKey, appSecret) // 실패 시 여기서 상세 예외가 던져짐

        val res = try {
            api.getOverseasPrice(
                authorization = "Bearer $token",
                appKey = appKey,
                appSecret = appSecret,
                excd = excd,
                symb = ticker
            )
        } catch (e: Exception) {
            throw IllegalStateException("[$ticker] 시세 조회 네트워크 오류: ${e.message}")
        }

        if (!res.isSuccessful) {
            val errBody = res.errorBody()?.string()
            throw IllegalStateException("[$ticker] 시세 조회 실패 (HTTP ${res.code()}): ${errBody ?: res.message()}")
        }

        val body = res.body()
        if (body?.resultCode != "0") {
            throw IllegalStateException("[$ticker] KIS 응답 오류 (rt_cd=${body?.resultCode}): ${body?.message}")
        }

        val output = body.output
            ?: throw IllegalStateException("[$ticker] 응답에 output 없음")
        val last = output.last?.toDoubleOrNull()
            ?: throw IllegalStateException("[$ticker] 가격 파싱 실패: last=${output.last}")
        val rate = output.rate?.toDoubleOrNull() ?: 0.0
        val isPositive = when (output.sign) {
            "1", "2" -> true
            "4", "5" -> false
            else -> rate >= 0.0
        }
        return LivePrice(
            price = formatPrice(last),
            changePercent = "${if (isPositive) "+" else ""}${"%.2f".format(rate)}%",
            isPositive = isPositive
        )
    }

    /** 기존 호출부 호환용 - 실패 시 null */
    suspend fun fetchPrice(ticker: String): LivePrice? = runCatching { fetchPriceOrThrow(ticker) }.getOrNull()

    private fun formatPrice(value: Double): String {
        return if (value >= 1000) "%,.2f".format(value) else "%.2f".format(value)
    }
}
