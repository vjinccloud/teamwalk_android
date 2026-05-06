package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.taiwanlife.teamwalk.R
import timber.log.Timber

open class MyWebChromeClient(private val context: Context) : WebChromeClient() {
    companion object {
        // 0002992 現場診斷模式：QA 看不到 logcat，改用 Toast 把流程關鍵節點顯示在螢幕上
        // 確認 bug 收斂後可改 false 或拔掉
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
        // #0002992 簡化版：直接 Toast 呈現訊息 + 立即 confirm，不再嘗試 AlertDialog
        // 之前 AlertDialog 在某些情境（Pixel 7 也會）silent 失敗或顯示不出來，
        // 流程也會卡住。改成直接 Toast 確保 user 一定看得到訊息，並立即 confirm
        // 讓 webview 的 JS 不會被卡住。
        Timber.d("0002992 onJsAlert: url=$url | msg=$message")
        Toast.makeText(context, "📢 ${message ?: ""}", Toast.LENGTH_LONG).show()
        alertCallback?.invoke()
        result.confirm()
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
            AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    confirmCallback?.invoke(true)
                    result.confirm()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    confirmCallback?.invoke(false)
                    result.cancel()
                }
                .setCancelable(false)
                .show()
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