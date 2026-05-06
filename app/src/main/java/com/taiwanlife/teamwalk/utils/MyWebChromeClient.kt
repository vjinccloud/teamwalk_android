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
        // #0002992 現場診斷：先 toast 證明 onJsAlert 有觸發
        if (DIAG_TOAST) {
            Toast.makeText(context, "[診斷] onJsAlert 觸發\nmsg=$message", Toast.LENGTH_LONG).show()
        }
        Timber.d("0002992 onJsAlert: url=$url | msg=$message")

        // #0002992 改進方案：先嘗試 AlertDialog，並透過 isShowing 確認是否真的顯示。
        // 若 isShowing 為 false（show() 沒拋錯但 dialog 沒被 attach 到 window），
        // 改用 Toast 把訊息呈現給使用者，同時自動 confirm 不讓 webview 的 JS 卡住。
        var dialogShownVisible = false
        try {
            val dialog = AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    alertCallback?.invoke()
                    result.confirm()
                }
                .setCancelable(false)
                .create()
            dialog.show()
            dialogShownVisible = dialog.isShowing
            if (DIAG_TOAST) {
                Toast.makeText(
                    context,
                    "[診斷] AlertDialog show() 完成，isShowing=$dialogShownVisible",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Timber.e(e, "onJsAlert show dialog failed")
            if (DIAG_TOAST) {
                Toast.makeText(
                    context,
                    "[診斷] AlertDialog 失敗\n${e.javaClass.simpleName}: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // 若 AlertDialog 沒成功顯示，用 Toast 呈現訊息 + 自動 confirm 讓 webview 流程繼續
        if (!dialogShownVisible) {
            Toast.makeText(
                context,
                "📢 ${message ?: ""}",
                Toast.LENGTH_LONG
            ).show()
            alertCallback?.invoke()
            result.confirm()
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