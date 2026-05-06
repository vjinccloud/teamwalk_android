package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.taiwanlife.teamwalk.R
import timber.log.Timber

open class MyWebChromeClient(private val context: Context) : WebChromeClient() {
    companion object {
        // 0002992 現場診斷模式：QA 看不到 logcat，改用 Toast 把流程關鍵節點顯示在螢幕上
        // LoginActivity 流程追蹤用，確認 bug 收斂後可改 false 或拔掉
        const val DIAG_TOAST = true
    }

    private var alertCallback: (() -> Unit)? = null
    private var confirmCallback: ((isConfirm: Boolean) -> Unit)? = null

    @Override
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        Timber.d("0002992 onJsAlert: url=$url | msg=$message")

        // #0002992: 「密碼超過三個月」的 CSSO 提醒會卡住 LoginActivity 流程
        // （dialog 在 1dp×1dp webview 觸發環境下顯示異常 + 即使顯示 user 點 OK 後本來就要跳改密碼頁），
        // 對 user 來說多一個提醒沒實質意義且增加白屏風險。
        // 偵測到此類 alert 直接 confirm 讓 webview 跳改密碼頁，由 CSSOWebViewActivity 統一接續流程。
        // 比對策略：訊息同時包含「三個月」與「密碼」兩個 keyword，誤判機率極低。
        if (message != null && message.contains("三個月") && message.contains("密碼")) {
            Timber.d("0002992 偵測到密碼三個月提醒 alert，直接 confirm 讓 webview 跳改密碼頁")
            result.confirm()
            return true
        }

        // 其他 alert（密碼錯誤、驗證碼錯誤等）走正常 dialog 流程
        // 用跟 CSSOWebViewActivity 一樣的 AlertDialogManager（系統版 android.app.AlertDialog），
        // 避免 androidx.appcompat.AlertDialog 在 1dp×1dp webview 環境 attach 失敗
        try {
            AlertDialogManager.getAlertDialog(
                context,
                message ?: "",
                false,
                true,
                context.getString(R.string.ok),
                {
                    alertCallback?.invoke()
                    result.confirm()
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "onJsAlert show dialog failed, fallback to result.cancel()")
            result.cancel()
        }
        return true
    }

    @Override
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        try {
            AlertDialogManager.getAlertDialog(
                context,
                message ?: "",
                false,
                true,
                context.getString(R.string.ok),
                {
                    confirmCallback?.invoke(true)
                    result.confirm()
                },
                context.getString(R.string.cancel),
                {
                    confirmCallback?.invoke(false)
                    result.cancel()
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "onJsConfirm show dialog failed, fallback to result.cancel()")
            result.cancel()
        }
        return true
    }

    fun setAlertCallback(alertCallback: (() -> Unit)) {
        this.alertCallback = alertCallback
    }

    fun setConfirmCallback(confirmCallback: (isConfirm: Boolean) -> Unit) {
        this.confirmCallback = confirmCallback
    }
}
