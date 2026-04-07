package com.taiwanlife.teamwalk.utils

import android.app.Activity
import android.content.Context
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.taiwanlife.teamwalk.R

open class MyWebChromeClient(private val context: Context) : WebChromeClient() {
    private var alertCallback: (() -> Unit)? = null
    private var confirmCallback: ((isConfirm: Boolean) -> Unit)? = null

    private fun isContextValid(): Boolean {
        val activity = context as? Activity
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }

    @Override
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        if (!isContextValid()) {
            result.cancel()
            return true
        }

        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(
                R.string.ok
            ) { dialog, which ->
                alertCallback?.invoke()
                result.confirm()
            }
            .setCancelable(false)
            .show()

        return true
    }

    @Override
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        if (!isContextValid()) {
            result.cancel()
            return true
        }

        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(
                R.string.ok
            ) { dialog, which ->
                confirmCallback?.invoke(true)
                result.confirm()
            }
            .setNegativeButton(R.string.cancel) { dialog, which ->
                confirmCallback?.invoke(false)
                result.cancel()
            }
            .setCancelable(false)
            .show()

        return true
    }

    fun setAlertCallback(alertCallback: (() -> Unit)) {
        this.alertCallback = alertCallback
    }

    fun setConfirmCallback(confirmCallback: (isConfirm: Boolean) -> Unit) {
        this.confirmCallback = confirmCallback
    }
}