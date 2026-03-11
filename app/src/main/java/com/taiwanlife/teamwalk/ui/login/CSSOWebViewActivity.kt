package com.taiwanlife.teamwalk.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.google.gson.Gson
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityCssoWebviewBinding
import com.taiwanlife.teamwalk.java_utils.SensitiveDataUtil
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.utils.AlertDialogManager
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.SecurityCheckManager
import com.taiwanlife.teamwalk.utils.Utils
import timber.log.Timber
import java.util.Locale

class CSSOWebViewActivity :
    BaseActivity<ActivityCssoWebviewBinding>({ ActivityCssoWebviewBinding.inflate(it) }) {

    companion object {
        private const val KEY_PURPOSE: String = "KEY_PURPOSE"
        private const val PURPOSE_REGISTER: String = "REGISTER"
        private const val PURPOSE_FORGET_PASSWORD: String = "FORGET_PASSWORD"
        private const val PURPOSE_NOTIFY_CHANGE_PASSWORD: String = "NOTIFY_CHANGE_PASSWORD"


        // 進來是為了註冊
        fun register(context: Context?): Intent {
            val intent = Intent(context, CSSOWebViewActivity::class.java)
            intent.putExtra(KEY_PURPOSE, PURPOSE_REGISTER)
            return intent
        }

        // 進來是忘了密碼
        fun forgetPassword(context: Context?): Intent {
            val intent = Intent(context, CSSOWebViewActivity::class.java)
            intent.putExtra(KEY_PURPOSE, PURPOSE_FORGET_PASSWORD)
            return intent
        }

        // 進來是提醒要更新密碼
        fun notifyChangePassword(context: Context): Intent {
            val intent = Intent(context, CSSOWebViewActivity::class.java)
            intent.putExtra(KEY_PURPOSE, PURPOSE_NOTIFY_CHANGE_PASSWORD)
            return intent
        }
    }

    override val statusBarColor: Int = R.color.colorCTBCPrimary

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {

        webView = viewBinding.webview

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true


        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true


        // Enable pinch to zoom without the zoom buttons
        webSettings.builtInZoomControls = false

        // Hide the zoom controls for HONEYCOMB+
        webSettings.displayZoomControls = false


        webView.webViewClient = CSSOWebViewClient(this)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String,
                result: JsResult
            ): Boolean {
                AlertDialogManager.getAlertDialog(
                    this@CSSOWebViewActivity,
                    message,
                    false,
                    true,
                    getString(R.string.confirm2), {
                        result.confirm()
                    })
                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String,
                result: JsResult
            ): Boolean {
                AlertDialogManager.getAlertDialog(
                    this@CSSOWebViewActivity,
                    message,
                    false,
                    true,
                    getString(R.string.confirm2), {
                        result.confirm()
                    },
                    getString(R.string.cancel), {
                        result.cancel()
                    })
                return true
            }
        }

        var cssoUrl = "about:blank"
        val purpose = intent.getStringExtra(KEY_PURPOSE)
        if (purpose.isNullOrEmpty()) {
            finish()
            return
        }
        when (purpose) {
            PURPOSE_REGISTER -> {
                cssoUrl = getEnvironmentConfig().cssoSignUpUrl
            }
            PURPOSE_FORGET_PASSWORD -> {
                cssoUrl = getEnvironmentConfig().cssoForgetMimaUrl
            }
            PURPOSE_NOTIFY_CHANGE_PASSWORD -> {
                val changeParamsJson =
                    SecuredPreferenceStoreManager.getString(Config.SP_CHANGE_PARAMS, "")
                if (changeParamsJson.isEmpty()) {
                    finish()
                    return
                }

                val changeParams = Gson().fromJson(changeParamsJson, ChangeParams::class.java)
                SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_CHANGE_PARAMS, "")
                cssoUrl = getEnvironmentConfig().cssoUrl.toUri() // 我們寫死的CSSO網域 例：https://csso.taiwanlife.com/csso/
                    .buildUpon()
                    .appendPath(Config.CHANGE_PATH) // 加上變更密碼的Path，和上面的網址組完後為可信任的CSSO網址 例：https://csso.taiwanlife.com/csso/mobileChgPwd
                    .appendQueryParameter(LoginActivity.QUERY_PARAM_SERVICE_ID, changeParams.serviceId) // 加上解密後的參數，用來比對來源
                    .appendQueryParameter(LoginActivity.QUERY_PARAM_TICKET, changeParams.ticket) // 加上解密後的參數，用來確認使用者
                    .build()
                    .toString()
            }
        }


        webView.loadUrl(cssoUrl)
//        webView.loadUrl("https://csso.taiwanlife.com/csso/mobileForget?outsite=teamwalk2")
//        webView.loadUrl("https://csso.taiwanlife.com/csso/mobileRegister?outsite=teamwalk2")
//        webView.postDelayed({
//            webView.evaluateJavascript( "window.location.href = 'https://demo.mutron.com.tw/login';", null)
//        }, 1000L)
//        webView.postDelayed({
//            // 組裝 JavaScript 字串：先跳 Alert，按下確定後執行 window.location.href
//            val jsCode = """
//        alert('驗證成功！即將返回 App。');
//        window.location.href = 'https://demo.mutron.com.tw/login';
//    """.trimIndent()
//
//            webView.evaluateJavascript(jsCode, null)
//        }, 1000L)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!backIfValid()) {
                    Utils.clearLoginData(this@CSSOWebViewActivity, webView)

                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun backIfValid(): Boolean {
        return if (webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    override fun onResume() {
        super.onResume()

        val emulatorResult = SecurityCheckManager.runShutdownCheck(this)
        if (!emulatorResult.passed) {
            getAlertDialog(
                context = this,
                message = emulatorResult.errorMessage ?: "",
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.confirm1),
                positiveOnClick = {
                    finishAffinity()
                }
            )
            return
        }
        val result = SecurityCheckManager.runSecurityCheck(this)
        if (!result.passed) {
            getAlertDialog(
                context = this,
                message = result.errorMessage ?: "",
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {}
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearSensitiveData(true)
    }

    override fun onStart() {
        super.onStart()
        restoreSensitiveData()
//        CelebrusCSAUtil.start(this);
    }

    override fun onStop() {
        super.onStop()
        backupSensitiveData()
        clearSensitiveData(false)
    }

    /**
     * @param keyCode
     * @param event
     * @return
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun backupSensitiveData() {
    }

    private fun restoreSensitiveData() {
    }

    private fun clearSensitiveData(isDestroy: Boolean) {
        if (isDestroy) {
            webView!!.loadUrl("about:blank")
            SensitiveDataUtil.clearWebViewSensitiveData(this, webView, isDestroy)
        }
    }

    private class CSSOWebViewClient(private val context: Context) : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            return urlLoading(view, url.toUri())
        }

        /**
         * @param view
         * @param request
         * @return
         */
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            return urlLoading(view, request.url)
        }

        private fun urlLoading(view: WebView, uri: Uri): Boolean {
            if (Utils.isAppLink(uri)) {
                try {
                    val pm = context.packageManager
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }

                    val activities = pm.queryIntentActivities(intent, 0)
                    if (activities.isEmpty()) {
                        // 如果沒有能處理的APP 關閉註冊/忘記密碼回到登入頁
                        if (context is Activity) {
                            context.finish()
                        }
                    } else {
                        val chooser = Intent.createChooser(intent, "選擇開啟方式")
                        context.startActivity(chooser)
                        if (context is Activity) {
                            context.finish()
                        }
                    }

                } catch (e: Exception) {
                    // 如果沒有能處理的APP 關閉註冊/忘記密碼回到登入頁
                    if (context is Activity) {
                        context.finish()
                    }
                }
                return true
            }

            val cssoURL = getEnvironmentConfig().cssoUrl.toUri()
            if (TextUtils.equals(cssoURL.host, uri.host)) {
                if (TextUtils.equals("/csso/mobileIndex", uri.path)) {
                    if (context is Activity) {
                        context.finish()
                    }
                    return true
                }

                return false
            }

            if (uri.host!!.contains("bid.g.doubleclick.net")) {
                return false
            }

//            if(request.url.toString() == "teamwalk://login") {
//                if (context is Activity) {
//                    context.finish()
//                }
//            }

            uri.toString().let { safeLoadUrl(view, it) }

            return true
        }

        private fun safeLoadUrl(view: WebView?, url: String) {
            val uri = url.toUri()
            val host = uri.host?.lowercase() ?: return

            val isAllowed = Utils.isAllowedHost(host)

            if (isAllowed) {
                view?.loadUrl(url)
            } else {
                unSafeUrl(url)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Timber.d("Start loading $url")
            val uri = url?.toUri() ?: return
            val host = uri.host?.lowercase() ?: return

            val isAllowed = Utils.isAllowedHost(host)
            if (!isAllowed) {
                view?.stopLoading()
                unSafeUrl(url)
                return
            }
        }

        @Override
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            if (request?.url?.scheme?.startsWith(Config.WEBVIEW_CALLBACK_SCHEME) == true) {
                return
            }
            // 確保錯誤是針對主框架的請求 (isForMainFrame)
            if (request?.isForMainFrame == true) {
                val description = error?.description.toString()
                val errorCode = error?.errorCode ?: -1

                AlertDialog.Builder(context)
                    .setTitle(
                        String.format(
                            Locale.getDefault(),
                            context.getString(R.string.webview_error_title),
                            errorCode.toString()
                        )
                    )
                    .setMessage(
                        String.format(
                            Locale.getDefault(),
                            context.getString(R.string.webview_error_message),
                            description
                        )
                    )
                    .setPositiveButton(R.string.confirm1) { dialog, _ ->

                    }
                    .setCancelable(true)
                    .show()
            }
        }

        private fun unSafeUrl(url: String) {
            CommonDialog(context).apply {
                oneButtonInit(
                    context.getString(R.string.webview_not_allowed_host),
                    url,
                    R.drawable.alert_1,
                    showButtons = true,
                    canceledOnTouchOutside = true,
                    text = context.getString(R.string.ok),
                    onClick = {
                        (this@CSSOWebViewClient.context as Activity).finish()
                    }
                )
            }.show()
        }
    }
}