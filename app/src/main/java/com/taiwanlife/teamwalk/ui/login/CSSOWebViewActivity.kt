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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityCssoWebviewBinding
import com.taiwanlife.teamwalk.java_utils.SensitiveDataUtil
import com.taiwanlife.teamwalk.ui.app_links.AppLinksEntryActivity
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.utils.AlertDialogManager
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.SecurityCheckManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.Utils.clearSensitiveData
import timber.log.Timber
import java.util.Locale

class CSSOWebViewActivity :
    BaseActivity<ActivityCssoWebviewBinding>({ ActivityCssoWebviewBinding.inflate(it) }) {

    companion object {
        private const val KEY_PURPOSE: String = "KEY_PURPOSE"
        private const val KEY_URL: String = "KEY_URL"
        private const val PURPOSE_REGISTER: String = "REGISTER"
        private const val PURPOSE_FORGET_PASSWORD: String = "FORGET_PASSWORD"
        private const val PURPOSE_NOTIFY_CHANGE_PASSWORD: String = "NOTIFY_CHANGE_PASSWORD"
        private const val PURPOSE_GARMIN: String = "GARMIN"

        // #0002992 loading 防呆：8 秒沒結束強制關掉
        private const val LOADING_TIMEOUT_MS = 8_000L


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

        // 進來是綁定 Garmin：改在 App 內 WebView 完成授權，
        // callback 由 shouldOverrideUrlLoading 直接攔進 AppLinksEntryActivity，不依賴系統 App Link 驗證
        fun garmin(context: Context, url: String): Intent {
            val intent = Intent(context, CSSOWebViewActivity::class.java)
            intent.putExtra(KEY_PURPOSE, PURPOSE_GARMIN)
            intent.putExtra(KEY_URL, url)
            return intent
        }
    }

    override val statusBarColor: Int = R.color.colorCTBCPrimary

    private lateinit var webView: WebView

    // #0002992 loading timeout 保險，避免 onPageFinished 沒觸發導致 loading 卡住
    private var loadingTimeoutJob: Job? = null

    private fun startLoadingTimeoutWatcher() {
        cancelLoadingTimeoutWatcher()
        loadingTimeoutJob = lifecycleScope.launch {
            delay(LOADING_TIMEOUT_MS)
            onLoading(false)
        }
    }

    private fun cancelLoadingTimeoutWatcher() {
        loadingTimeoutJob?.cancel()
        loadingTimeoutJob = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {

        val purpose = intent.getStringExtra(KEY_PURPOSE)
        if (purpose.isNullOrEmpty()) {
            finish()
            return
        }
        val isGarmin = purpose == PURPOSE_GARMIN

        webView = viewBinding.webview

        val webSettings = webView.settings
        // 0003005: CSSO 透過 user-agent 判斷是否為 teamwalk app
        // Garmin 授權頁不加，避免被判定為非標準瀏覽器
        if (!isGarmin) {
            webSettings.userAgentString = webSettings.userAgentString + "/env=taiwanlife_teamwalk_app"
        }
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // 0003003: 不受系統字級影響，避免大字級導致跑版
        webSettings.textZoom = 100


        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true


        // Enable pinch to zoom without the zoom buttons
        webSettings.builtInZoomControls = false

        // Hide the zoom controls for HONEYCOMB+
        webSettings.displayZoomControls = false


        webView.webViewClient = CSSOWebViewClient(this, isGarmin, object : WebviewLoadingCallback{
            override fun onWebviewPageStarted() {
                onLoading(true)
                // #0002992 timeout 保險：8 秒沒收到結束信號，自動關 loading
                startLoadingTimeoutWatcher()
            }

            override fun onWebviewPageFinished() {
                onLoading(false)
                cancelLoadingTimeoutWatcher()
            }
        })
        webView.webChromeClient = object : WebChromeClient() {
            // #0002992 修正：onPageFinished 在某些頁面（多重 redirect / iframe / JS-heavy）
            // 不一定可靠觸發，導致 loading「連線中」永遠不消失。
            // 改用 onProgressChanged 100% 為「載入完成」備援，只要進度到底就主動關 loading。
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress >= 100) {
                    onLoading(false)
                    cancelLoadingTimeoutWatcher()
                }
            }

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

        var cssoUrl = ""
        when (purpose) {
            PURPOSE_GARMIN -> {
                cssoUrl = intent.getStringExtra(KEY_URL).orEmpty()
                if (cssoUrl.isEmpty()) {
                    finish()
                    return
                }
            }
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
                    // Garmin 綁定時使用者已登入，取消授權不可清除登入資料與既有綁定
                    if (!isGarmin) {
                        Utils.clearLoginData(this@CSSOWebViewActivity, webView)
                    }

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


    interface WebviewLoadingCallback {
        fun onWebviewPageStarted()
        fun onWebviewPageFinished()
    }

    private class CSSOWebViewClient(
        private val context: Context,
        private val isGarmin: Boolean,
        private val webviewLoadingCallback: WebviewLoadingCallback
    ) : WebViewClient() {
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

        // shouldOverrideUrlLoading 在部分版本不會對 302 轉導觸發，
        // onPageStarted 也會再攔一次，用此旗標避免重複開啟
        private var appLinkHandled = false

        private fun handleAppLink(uri: Uri): Boolean {
            if (appLinkHandled) return true
            appLinkHandled = true
            try {
                val intent = Intent(context, AppLinksEntryActivity::class.java).apply {
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e, "導向 AppLinksEntryActivity 失敗")
            } finally {
                if (context is Activity) {
                    context.finish()
                }
            }
            return true
        }

        private fun urlLoading(view: WebView, uri: Uri): Boolean {
            if (Utils.isAppLink(uri)) {
                return handleAppLink(uri)
            }

            // Garmin 授權流程交給 WebView 自己導頁，不要接手 loadUrl（會把登入的 POST 變成 GET）
            if (isGarmin && Utils.isGarminHost(uri.host)) {
                return false
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

            // 302 轉導未觸發 shouldOverrideUrlLoading 時的備援：在頁面真正載入前攔下 callback
            if (Utils.isAppLink(uri)) {
                view?.stopLoading()
                handleAppLink(uri)
                return
            }

            // Garmin 綁定時額外放行 garmin.com，其他情境維持原本白名單
            val isAllowed = Utils.isAllowedHost(host) || (isGarmin && Utils.isGarminHost(host))
            if (!isAllowed) {
                view?.stopLoading()
                unSafeUrl(url)
                return
            }
            webviewLoadingCallback.onWebviewPageStarted()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            webviewLoadingCallback.onWebviewPageFinished()
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
                // #0002992 修正：error 時也要關掉 loading，否則「連線中」永遠不消失
                webviewLoadingCallback.onWebviewPageFinished()

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
                    .setPositiveButton(R.string.confirm1) { _, _ ->
                        // #0002992 修正：使用者按確定後關閉 CSSOWebViewActivity，避免停留在「連線中」畫面
                        (context as? Activity)?.finish()
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