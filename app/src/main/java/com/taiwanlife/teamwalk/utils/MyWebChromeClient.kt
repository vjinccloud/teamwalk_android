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

        // #0002992: 改用跟 CSSOWebViewActivity 一樣的 AlertDialogManager（系統版 android.app.AlertDialog），
        // 因為原本的 androidx.appcompat.AlertDialog 在 LoginActivity 的 1dp×1dp webview 觸發下
        // attach 不到 window，但 CSSOWebViewActivity 用同一個 AlertDialogManager 確認可以正常顯示。
        // 統一用同樣 dialog 機制，避免兩邊行為不一致。
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
