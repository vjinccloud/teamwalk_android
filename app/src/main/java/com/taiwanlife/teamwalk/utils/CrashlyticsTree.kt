package com.taiwanlife.teamwalk.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.taiwanlife.teamwalk.remote.interceptor.ApiLoggingInterceptor
import timber.log.Timber

/**
 * 把既有的 Timber log 轉成 Crashlytics 的麵包屑，crash 發生時在 Console 上
 * 看得到前因後果，不必回頭在程式裡一個一個補埋點。
 *
 * 專案內既有 49 個 Timber 呼叫點（24 個 d、1 個 i、6 個 w、18 個 e）掛上這棵樹後
 * 自動全部生效。
 *
 * 刻意排除 [ApiLoggingInterceptor] 的 API_LOG：那些內容含完整 request/response body
 * 與未遮罩的 Authorization / JWT header（見該類別的 formatHeaders），送到 Firebase
 * 等於外洩使用者憑證。API 軌跡改由 [com.taiwanlife.teamwalk.remote.interceptor.CrashlyticsApiInterceptor]
 * 送出，只帶 method 與 path。
 *
 * 網址的 query 一律拿掉只留路徑：MyWebView / CSSOWebViewActivity 會印出完整載入網址，
 * Garmin / Fitbit 綁定導回的 ?code=、CSSO 導回的 ?ticket= 都是一次性憑證，不能送出去。
 *
 * ERROR 等級且帶 Throwable 的會額外以非致命錯誤回報，在 Console 上獨立成一群。
 */
class CrashlyticsTree : Timber.Tree() {

    companion object {
        private val URL_QUERY = Regex("""\?\S*""")
    }

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return tag != ApiLoggingInterceptor.apiTag
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val level = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }
        val safeMessage = message.replace(URL_QUERY, "?…")
        crashlytics.log(if (tag.isNullOrEmpty()) "$level: $safeMessage" else "$level/$tag: $safeMessage")

        if (priority >= Log.ERROR && t != null) {
            crashlytics.recordException(t)
        }
    }
}
