package com.taiwanlife.teamwalk.remote.interceptor

import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 送 API 軌跡與失敗事件到 Crashlytics。
 *
 * 跟 [ApiLoggingInterceptor] 分開而不是加在它裡面，有兩個原因：
 *   1. 那支只在 BuildConfig.ENABLE_API_LOG 為 true 時才掛（見 AppModule），release
 *      版沒裝。接在那裡等於正式機收不到任何回報。這支一律掛載。
 *   2. 那支印完整 header 與 body（含未遮罩的 JWT），內容不能送到 Firebase。
 *      這支只帶 method 與 path，不帶 query、header、body。
 *
 * 回報門檻：
 *   - 連不上 / timeout 等網路例外 → 非致命回報，這種最需要被看到
 *   - HTTP 5xx → 非致命回報，後端出事
 *   - HTTP 4xx → 只留麵包屑。401 token 過期屬日常，報上去會洗版蓋掉真正的問題
 */
class CrashlyticsApiInterceptor : Interceptor {

    companion object {
        private const val KEY_LAST_API = "last_api"
    }

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 只取 method + path。query 可能夾帶 ticket / token，不能送出去
        val endpoint = "${request.method} ${request.url.encodedPath}"

        crashlytics.setCustomKey(KEY_LAST_API, endpoint)
        crashlytics.log("API -> $endpoint")

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            crashlytics.log("API x $endpoint (${e.javaClass.simpleName})")
            crashlytics.recordException(ApiCallException(endpoint, e))
            throw e
        }

        crashlytics.log("API <- ${response.code} $endpoint")
        if (response.code >= 500) {
            crashlytics.recordException(ApiCallException("$endpoint -> HTTP ${response.code}"))
        }
        return response
    }
}

/**
 * 只是要讓 Crashlytics 上的非致命錯誤有個好認的名字，不對外拋、不參與
 * [com.taiwanlife.teamwalk.remote.ApiException] 那條既有的錯誤處理鏈。
 */
class ApiCallException(message: String, cause: Throwable? = null) : Exception(message, cause)
