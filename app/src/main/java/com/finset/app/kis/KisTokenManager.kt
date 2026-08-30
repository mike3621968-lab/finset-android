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

    suspend fun getValidToken(appKey: String, appSecret: String): String? {
        val cached = prefs.getString(KEY_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val now = System.currentTimeMillis()

        // 만료 10분 전까지는 캐시된 토큰 재사용
        if (cached != null && now < expiresAt - 10 * 60 * 1000) {
            return cached
        }

        return runCatching {
            val res = api.issueToken(KisTokenRequest(appkey = appKey, appsecret = appSecret))
            if (res.isSuccessful) {
                val body = res.body()
                val token = body?.accessToken
                val expiresInSec = body?.expiresIn ?: (24 * 60 * 60L)
                if (token != null) {
                    prefs.edit()
                        .putString(KEY_TOKEN, token)
                        .putLong(KEY_EXPIRES_AT, now + expiresInSec * 1000)
                        .apply()
                }
                token
            } else null
        }.getOrNull()
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
