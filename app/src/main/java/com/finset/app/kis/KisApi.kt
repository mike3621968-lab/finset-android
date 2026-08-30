package com.finset.app.kis

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface KisApi {

    @Headers("Content-Type: application/json; charset=UTF-8")
    @retrofit2.http.POST("oauth2/tokenP")
    suspend fun issueToken(@Body body: KisTokenRequest): Response<KisTokenResponse>

    /**
     * 해외주식 현재가 조회
     * EXCD: 거래소코드 (NAS=나스닥, NYS=뉴욕, AMS=아멕스)
     * SYMB: 종목코드 (예: AAPL)
     */
    @GET("uapi/overseas-price/v1/quotations/price")
    suspend fun getOverseasPrice(
        @Header("authorization") authorization: String, // "Bearer {access_token}"
        @Header("appkey") appKey: String,
        @Header("appsecret") appSecret: String,
        @Header("tr_id") trId: String = "HHDFS00000300",
        @Header("custtype") custType: String = "P",
        @Query("AUTH") auth: String = "",
        @Query("EXCD") excd: String,
        @Query("SYMB") symb: String
    ): Response<KisOverseasPriceResponse>
}
