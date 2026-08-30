package com.finset.app.kis

import android.content.Context
import android.content.SharedPreferences

/**
 * KIS 접근토큰은 유효기간이 24시간이고, 너무 자주 재발급하면 이용이 제한될 수 있어
 * SharedPreferences에 캐싱해두고 만료 전까지는 재사용한다.
 */
class KisTokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kis_token_prefs", Context.MODE_PRIVATE)

    private val api: KisApi by lazy { KisApiClient.create() }

    /** 토큰 발급/조회에 실패하면 사유를 담은 예외를 던진다 (성공 시에만 문자열 반환) */
    suspend fun getValidToken(appKey: String, appSecret: String): String {
        val cached = prefs.getString(KEY_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val now = System.currentTimeMillis()

        // 만료 10분 전까지는 캐시된 토큰 재사용
        if (cached != null && now < expiresAt - 10 * 60 * 1000) {
            return cached
        }

        val res = try {
            api.issueToken(KisTokenRequest(appkey = appKey, appsecret = appSecret))
        } catch (e: Exception) {
            throw IllegalStateException("토큰 발급 네트워크 오류: ${e.message}")
        }

        if (!res.isSuccessful) {
            val errBody = res.errorBody()?.string()
            throw IllegalStateException("토큰 발급 실패 (HTTP ${res.code()}): ${errBody ?: res.message()}")
        }

        val body = res.body()
        val token = body?.accessToken
            ?: throw IllegalStateException("토큰 발급 실패: 응답에 access_token 없음 (${body})")

        val expiresInSec = body.expiresIn ?: (24 * 60 * 60L)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, now + expiresInSec * 1000)
            .apply()
        return token
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
