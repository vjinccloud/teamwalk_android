package com.taiwanlife.teamwalk.ui.main.webview

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleOwner
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.ui.main.webview.MyWebAppInterface.AsyncCallbacks
import timber.log.Timber
import androidx.core.net.toUri
import com.taiwanlife.teamwalk.R

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

    fun setUp(
        lifecycleOwner: LifecycleOwner,
        webviewLoadingCallback: WebviewLoadingCallback,
        asyncCallbacks: AsyncCallbacks,
        fileChooserCallback: (
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ) -> Unit
    ) {
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

        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true


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

        webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {

                fileChooserCallback(filePathCallback, fileChooserParams)
                return true
            }
        }

        setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(url.toUri()).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )
            }

            val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

//            Toast.makeText(context, context.getString(R.string.webview_start_download), Toast.LENGTH_SHORT).show()
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