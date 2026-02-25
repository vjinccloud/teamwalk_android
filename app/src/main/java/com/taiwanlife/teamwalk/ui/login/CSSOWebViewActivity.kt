package com.taiwanlife.teamwalk.ui.login

import android.R.attr.host
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
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityCssoWebviewBinding
import com.taiwanlife.teamwalk.java_utils.SensitiveDataUtil
import com.taiwanlife.teamwalk.utils.AlertDialogManager
import androidx.core.net.toUri
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.MyApplication
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import timber.log.Timber
import java.util.Locale

class CSSOWebViewActivity :
    BaseActivity<ActivityCssoWebviewBinding>({ ActivityCssoWebviewBinding.inflate(it) }) {

    companion object {
        private const val KEY_PURPOSE: String = "KEY_PURPOSE"
        private const val PURPOSE_REGISTER: String = "REGISTER"
        private const val PURPOSE_FORGET_PASSWORD: String = "FORGET_PASSWORD"

        val CSSO_WEBVIEW_DOMAINS = setOf(
            MyApplication.context.getString(R.string.web_url).toUri().host,
            MyApplication.context.getString(R.string.api_url).toUri().host,
            MyApplication.context.getString(R.string.csso_url).toUri().host,
        ).filterNotNull() // 去掉 null

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

        var cssoURL = ""
        val purpose = intent.getStringExtra(CSSOWebViewActivity.KEY_PURPOSE)
        if (TextUtils.isEmpty(purpose)) {
            finish()
        } else if (TextUtils.equals(purpose, CSSOWebViewActivity.PURPOSE_REGISTER)) {
            cssoURL = getEnvironmentConfig().cssoSignUpUrl
        } else if (TextUtils.equals(purpose, CSSOWebViewActivity.PURPOSE_FORGET_PASSWORD)) {
            cssoURL = getEnvironmentConfig().cssoForgetMimaUrl
        } else {
            finish()
        }
        //        SharedPreferences loginSharedPref = getSharedPreferences(getString(R.string.pref_login), CSSOWebViewActivity.MODE_PRIVATE);
//        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
//        String csso = loginSharedPref.getString("csso", CSSO_SIGN_UP);
//
//        String cssoURL = EnvironmentManager.INSTANCE.getEnvironmentConfig().getCssoSignUpUrl();
//        if (TextUtils.equals(CSSO_SIGN_UP, csso)) {
//            cssoURL = EnvironmentManager.INSTANCE.getEnvironmentConfig().getCssoSignUpUrl();
//        }
//
//        if (TextUtils.equals(getString(R.string.csso_forget_pwd_key), csso)) {
//            cssoURL = EnvironmentManager.INSTANCE.getEnvironmentConfig().getCssoForgetMimaUrl();
//        }


        webView.loadUrl(cssoURL)
//        webView.loadUrl("https://csso.taiwanlife.com/csso/mobileForget?outsite=teamwalk")
//        webView.postDelayed({
//            webView.evaluateJavascript( "window.location.href = '${getString(R.string.redirect_scheme)}://login';", null)
//        }, 1000L)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!backIfValid()) {
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
        /**
         * @param view
         * @param request
         * @return
         */
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest
        ): Boolean {
//            Log.d(TAG, request.getUrl().toString());

            val cssoURL = getEnvironmentConfig().cssoUrl.toUri()
            if (TextUtils.equals(cssoURL.host,  request.url.host)) {
                if (TextUtils.equals("/csso/mobileIndex", request.url.path)) {
                    if (context is Activity) {
                        context.finish()
                    }
                    return true
                }

                return false
            }

            if (request.url.host!!.contains("bid.g.doubleclick.net")) {
                return false
            }

            Timber.d(request.url.toString())
            val intent = Intent(Intent.ACTION_VIEW, request.url)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
//            if(request.url.toString() == "teamwalk://login") {
//                if (context is Activity) {
//                    context.finish()
//                }
//            }
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            url?.toUri()?.host?.let { host ->
                if (!CSSO_WEBVIEW_DOMAINS.contains(host)) {
                    view?.stopLoading()
                    unSafeUrl(host)
                }
            }
        }

        @Override
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            if(request?.url?.scheme?.startsWith(Config.WEBVIEW_CALLBACK_SCHEME) == true) {
                return
            }
            // 確保錯誤是針對主框架的請求 (isForMainFrame)
            if (request?.isForMainFrame == true) {
                val description = error?.description.toString()
                val errorCode = error?.errorCode ?: -1

                AlertDialog.Builder(context)
                    .setTitle(String.format(Locale.getDefault(), context.getString(R.string.webview_error_title), errorCode.toString()))
                    .setMessage(String.format(Locale.getDefault(), context.getString(R.string.webview_error_message), description))
                    .setPositiveButton(R.string.confirm1) { dialog, _ ->

                    }
                    .setCancelable(true)
                    .show()
            }
        }

        private fun unSafeUrl(host: String) {
            CommonDialog(context).apply {
                oneButtonInit(
                    context.getString(R.string.webview_not_allowed_host),
                    host,
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