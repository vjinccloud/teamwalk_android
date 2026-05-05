package com.taiwanlife.teamwalk.remote.interceptor

import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {

    companion object {
        // OkHttp 客製 UA：加上 App 標記與版號，後端可依此辨識真實 App vs 外部偽造
        // 範例：okhttp/5.3.2 taiwanlife_teamwalk_app/3.0.41
        private val CUSTOM_USER_AGENT_SUFFIX =
            " taiwanlife_teamwalk_app/${BuildConfig.VERSION_NAME}"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request() // 取得原始請求

        // 檢查請求的路徑是否為登入路徑
        val url = originalRequest.url.toUrl().toString()
        val encodedUrl = originalRequest.url.encodedPath

        // 在所有 OkHttp 出去的 request 都補上客製 UA 後綴
        val baseUserAgent = originalRequest.header("User-Agent")
            ?: "okhttp"
        val customUserAgent = baseUserAgent + CUSTOM_USER_AGENT_SUFFIX

        // 使用 originalRequest.url.encodedPath 獲取編碼過的路徑，確保完整匹配
        if (encodedUrl.endsWith(Config.API_LOGIN_PATH)) {
            // 如果是登入路徑，直接放行，不添加 JWT，但仍補上 UA
            return chain.proceed(
                originalRequest.newBuilder()
                    .header("User-Agent", customUserAgent)
                    .build()
            )
        }
        if (!url.contains(EnvironmentManager.getEnvironmentConfig().apiUrl)) {
            // 如果不是API 也放行 不添加JWT，但仍補上 UA
            return chain.proceed(
                originalRequest.newBuilder()
                    .header("User-Agent", customUserAgent)
                    .build()
            )
        }
        val jwtToken = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT, "")
        if (jwtToken.isEmpty()) {
            // 沒有JWT Token 送出去 但是會出錯 請前往察看錯誤
            return chain.proceed(
                originalRequest.newBuilder()
                    .header("User-Agent", customUserAgent)
                    .build()
            )
        }

        val newRequest = originalRequest.newBuilder()
//            .header("Authorization", "Bearer $jwtToken") // 在 Authorization Header 中添加 Bearer Token
            .header("Authorization", jwtToken) // 在 Authorization Header 中添加 Bearer Token
            .header("User-Agent", customUserAgent)
            .build()

        return chain.proceed(newRequest)
    }
}