package com.taiwanlife.teamwalk.ui.main.webview

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.LifecycleOwner
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.ui.main.webview.MyWebAppInterface.AsyncCallbacks
import timber.log.Timber

class MyWebView : WebView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(context, attrs, defStyleAttr, privateBrowsing)

    interface WebviewLoadingCallback {
        fun onWebviewPageStarted()
        fun onWebviewPageFinished()
    }

    fun setUp(lifecycleOwner: LifecycleOwner, webviewLoadingCallback: WebviewLoadingCallback, asyncCallbacks: AsyncCallbacks) {
        val webSettings: WebSettings = settings
        webSettings.setUserAgentString(webSettings.userAgentString + "/env=taiwanlife_teamwalk_app")
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true


        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true


        // Enable pinch to zoom without the zoom buttons
        webSettings.builtInZoomControls = false

        // Hide the zoom controls for HONEYCOMB+
        webSettings.displayZoomControls = false


        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // chrome://inspect
        if (BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true)
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.toString()?.let { view?.loadUrl(it) }
                return true
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                Timber.d("Start loading $url")
                webviewLoadingCallback.onWebviewPageStarted()

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("Finish loading $url")
                webviewLoadingCallback.onWebviewPageFinished()
            }
        }


//        val webAppInterface = WebAppInterface(this)
//        addJavascriptInterface(webAppInterface, webAppInterface.appBridgeJsName)

        addJavascriptInterface(
            MyWebAppInterface(
                context,
                lifecycleOwner,
                this,
                asyncCallbacks
            ), Config.JAVASCRIPT_BRIDGE_NAME
        )

//        loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
    }

    fun backIfValid(): Boolean {
        if (canGoBack() && url != EnvironmentManager.getEnvironmentConfig().webUrl) {
            goBack()
            return true
        }
        return false
    }
}