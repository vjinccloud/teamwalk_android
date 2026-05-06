package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.taiwanlife.teamwalk.R
import timber.log.Timber

open class MyWebChromeClient(private val context: Context) : WebChromeClient() {

    private var alertCallback: (() -> Unit)? = null
    private var confirmCallback: ((isConfirm: Boolean) -> Unit)? = null

    @Override
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        // #0002992: 「密碼超過三個月」的 CSSO 提醒在 LoginActivity 1dp×1dp 隱形 webview
        // 觸發下 dialog 顯示異常，且 user 點 OK 後本來就要跳改密碼頁，多一個提醒沒實質
        // 意義。偵測到此類 alert 直接 confirm 讓 webview 跳改密碼頁，由 CSSOWebViewActivity
        // 接續流程。其他 alert（密碼錯誤、驗證碼錯誤等）走正常 dialog。
        if (message != null && message.contains("三個月") && message.contains("密碼")) {
            result.confirm()
            return true
        }

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
