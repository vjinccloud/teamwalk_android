package com.taiwanlife.teamwalk.ui.main.webview

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.Message
import android.util.AttributeSet
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.utils.AlertDialogManager
import com.taiwanlife.teamwalk.utils.MyWebChromeClient
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.Utils.openPlayStoreAndExit
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

    private var finishCallback: () -> Unit = {}
    private var loginCallback: () -> Unit = {}

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
        asyncCallbacks: MyWebMessageListener.AsyncCallbacks,
        loginCallback: () -> Unit,
        finishCallback: () -> Unit,
        fileChooserCallback: (
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
        ) -> Unit
    ) {
        this.finishCallback = finishCallback
        this.loginCallback = loginCallback
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
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return urlLoading(view, url.toUri())
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
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
                            // 如果沒有能處理的APP 回到登入頁
                            loginCallback()
                        } else {
                            val chooser = Intent.createChooser(intent, "選擇開啟方式")
                            context.startActivity(chooser)
                        }

                    } catch (e: Exception) {
                        // 如果沒有能處理的APP 回到登入頁
                        loginCallback()
                    }
                    return true
                }

                uri.toString().let { safeLoadUrl(view, it) }
                return true
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
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
                    val activity = context as? Activity
                    if (activity == null || activity.isFinishing || activity.isDestroyed) return

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

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                // 阻止新視窗
                return false
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

        val myWebMessageListener = MyWebMessageListener(context, lifecycleOwner, asyncCallbacks)
        myWebMessageListener.init(this)

//        loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
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
                    // 非授權網域 關閉APP
                    finishCallback()
                }
            )
        }.show()
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