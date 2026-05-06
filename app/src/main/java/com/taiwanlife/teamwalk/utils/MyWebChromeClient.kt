package com.taiwanlife.teamwalk.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
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
        Timber.d("0002992 onJsAlert: url=$url | msg=$message")

        val activity = context as? Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            // activity 已死，沒辦法顯示 dialog，回 cancel 不卡 JS
            if (DIAG_TOAST) {
                Toast.makeText(context, "[診斷] activity 已死，cancel alert", Toast.LENGTH_SHORT).show()
            }
            result.cancel()
            return true
        }

        if (DIAG_TOAST) {
            Toast.makeText(context, "[診斷-1] onJsAlert 觸發\nactivity=${activity.javaClass.simpleName}", Toast.LENGTH_SHORT).show()
        }

        // 強制丟到主 Looper 下一個 frame 跑，避開 webview callback 跟 UI thread 同 frame
        // 在某些 Android 版本可能造成 dialog window attach 競態的情境
        Handler(Looper.getMainLooper()).post {
            try {
                val dialog = AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        if (DIAG_TOAST) {
                            Toast.makeText(activity, "[診斷-3] user 按下 OK", Toast.LENGTH_SHORT).show()
                        }
                        alertCallback?.invoke()
                        result.confirm()
                    }
                    .setCancelable(false)
                    .create()
                dialog.show()

                if (DIAG_TOAST) {
                    Toast.makeText(
                        activity,
                        "[診斷-2] dialog.show() 完成\nisShowing=${dialog.isShowing}\nwindow=${dialog.window != null}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "onJsAlert show dialog failed")
                if (DIAG_TOAST) {
                    Toast.makeText(
                        activity,
                        "[診斷-2] dialog 失敗\n${e.javaClass.simpleName}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                result.cancel()
            }
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