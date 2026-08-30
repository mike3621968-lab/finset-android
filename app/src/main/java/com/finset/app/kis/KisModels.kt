package com.finset.app.kis

import com.google.gson.annotations.SerializedName

data class KisTokenRequest(
    val grant_type: String = "client_credentials",
    val appkey: String,
    val appsecret: String
)

data class KisTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Long?, // 초 단위, 보통 86400(24시간)
    @SerializedName("access_token_token_expired") val expiredAt: String?
)

/**
 * 해외주식 현재가 조회 (tr_id: HHDFS00000300) 응답의 output 블록.
 * 필드가 많아 실제 쓰는 것만 정의 (last=현재가, rate=등락율, diff=전일대비, sign=등락구분 1~2 상승/3 보합/4~5 하락)
 */
data class KisOverseasPriceResponse(
    @SerializedName("rt_cd") val resultCode: String?,   // "0" 이면 정상
    @SerializedName("msg1") val message: String?,
    val output: KisOverseasPriceOutput?
)

data class KisOverseasPriceOutput(
    @SerializedName("last") val last: String?,   // 현재가
    @SerializedName("diff") val diff: String?,   // 전일대비
    @SerializedName("rate") val rate: String?,   // 등락율(%)
    @SerializedName("sign") val sign: String?    // 1,2=상승 3=보합 4,5=하락
)
