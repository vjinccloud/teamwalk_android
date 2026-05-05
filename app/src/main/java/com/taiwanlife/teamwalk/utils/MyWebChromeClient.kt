package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
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
        // #0002992: 改用 try-catch 取代 isContextValid 預先檢查，避免 activity 處於
        // 「正在 transition」這種介於 alive 與 destroyed 之間的狀態被誤殺、
        // 同時保留對 BadTokenException 的防護（#0002957）
        try {
            AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    alertCallback?.invoke()
                    result.confirm()
                }
                .setCancelable(false)
                .show()
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