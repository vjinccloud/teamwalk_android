package com.taiwanlife.teamwalk.remote.interceptor

import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request() // 取得原始請求

        // 檢查請求的路徑是否為登入路徑
        val url = originalRequest.url.toUrl().toString()
        val encodedUrl = originalRequest.url.encodedPath
        // 使用 originalRequest.url.encodedPath 獲取編碼過的路徑，確保完整匹配
        if (encodedUrl.endsWith(Config.API_LOGIN_PATH)) {
            // 如果是登入路徑，直接放行，不添加 JWT
            return chain.proceed(originalRequest)
        }
        if(!url.contains(EnvironmentManager.getEnvironmentConfig().apiUrl)) {
            // 如果不是API 也放行 不添加JWT
            return chain.proceed(originalRequest)
        }
        val jwtToken = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT_TOKEN, "")
        if (jwtToken.isEmpty()) {
            // 沒有JWT Token 送出去 但是會出錯 請前往察看錯誤
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
//            .header("Authorization", "Bearer $jwtToken") // 在 Authorization Header 中添加 Bearer Token
            .header("Authorization", jwtToken) // 在 Authorization Header 中添加 Bearer Token
            .build()

        return chain.proceed(newRequest)
    }
}