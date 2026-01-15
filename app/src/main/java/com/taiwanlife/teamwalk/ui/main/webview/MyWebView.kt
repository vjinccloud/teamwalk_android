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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.main.webview.MyWebAppInterface.AsyncCallbacks
import com.taiwanlife.teamwalk.utils.MyWebChromeClient
import timber.log.Timber
import java.util.Locale

class MyWebView : WebView {

    companion object {
        const val CALLBACK_DEVICE_INFO_RESOLVER = "WebAppBridge.receiveDeviceInfo"
        const val CALLBACK_JWT_TOKEN_RESOLVER = "WebAppBridge.receiveLoginInfo"
        const val CALLBACK_APP_VERSION_RESOLVER = "WebAppBridge.receiveAppVersion"
        const val CALLBACK_GRAPHICAL_LOGIN_RESOLVER = "WebAppBridge.receiveGraphicalLoginResult"
        const val CALLBACK_BIND_GOOGLE_HEALTH_CONNECT_RESOLVER =
            "WebAppBridge.receiveBindingGoogleHealthResult"
        const val CALLBACK_BIND_GARMIN_HEALTH_RESOLVER = "WebAppBridge.receiveGarminHealthResult"
        const val CALLBACK_BIND_FITBIT_HEALTH_RESOLVER = "WebAppBridge.receiveFitbitHealthResult"
        const val CALLBACK_OPEN_NOTIFICATION_RESOLVER =
            "WebAppBridge.receiveSetPushMessageStatusResult"
        const val CALLBACK_SYNC_HEALTH_DATA_RESOLVER = "WebAppBridge.receiveSyncHealthDataResult"
        const val CALLBACK_CASTGC_RESOLVER = "WebAppBridge.receiveCastgcInfo"


        val LIST_OF_MAIN_PAGES = listOf(
            getEnvironmentConfig().webUrlBase + "main",
            getEnvironmentConfig().webUrlBase + "imei",
            getEnvironmentConfig().webUrlBase + "bridge",
        )
    }
//    companion object {
//        const val CALLBACK_DEVICE_INFO_RESOLVER = "deviceInfoResolver"
//        const val CALLBACK_JWT_TOKEN_RESOLVER = "jwtTokenResolver"
//        const val CALLBACK_APP_VERSION_RESOLVER = "appVersionResolver"
//        const val CALLBACK_GRAPHICAL_LOGIN_RESOLVER = "graphicalLoginResolver"
//        const val CALLBACK_BIND_GOOGLE_HEALTH_CONNECT_RESOLVER = "bindGoogleHealthConnectResolver"
//        const val CALLBACK_BIND_APPLE_IOS_HEALTH_RESOLVER = "bindAppleiOSHealthResolver"
//        const val CALLBACK_BIND_GARMIN_HEALTH_RESOLVER = "bindGarminHealthResolver"
//        const val CALLBACK_BIND_FITBIT_HEALTH_RESOLVER = "bindFitbitHealthResolver"
//        const val CALLBACK_OPEN_NOTIFICATION_RESOLVER = "openNotificationResolver"
//        const val CALLBACK_SYNC_HEALTH_DATA_RESOLVER = "syncHealthDataResolver"
//        const val CALLBACK_CASTGC_RESOLVER = "WebAppBridge.receiveCastgcInfo"
//    }

    private var finishCallback: () -> Unit = {}

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
        finishCallback: () -> Unit,
        fileChooserCallback: (
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ) -> Unit
    ) {
        this.finishCallback = finishCallback
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
        }

        webChromeClient = object : MyWebChromeClient(context) {
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

//        addJavascriptInterface(
//            MyWebAppInterface(
//                context,
//                lifecycleOwner,
//                this,
//                asyncCallbacks
//            ), Config.JAVASCRIPT_BRIDGE_NAME
//        )
        val myWebMessageListener = MyWebMessageListener(context, lifecycleOwner, asyncCallbacks)
        myWebMessageListener.init(this)

//        loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
    }

    fun backIfValid(): Boolean {
        if (!url.isNullOrEmpty()) {
            if (LIST_OF_MAIN_PAGES.any { mainUrl ->
                    url!!.startsWith(mainUrl)
                }) {
                finishCallback()
            }
        }
        if (canGoBack()) {
            goBack()
            return true
        }
        return false
    }
}