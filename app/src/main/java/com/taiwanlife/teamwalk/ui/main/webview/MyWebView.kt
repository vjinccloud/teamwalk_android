package com.taiwanlife.teamwalk.ui.main.webview

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.ui.main.webview.MyWebAppInterface

class MyWebView: WebView {
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

    fun setUp(asyncCallbacks : MyWebAppInterface.AsyncCallbacks) {
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

        // chrome://inspect
//        setWebContentsDebuggingEnabled(true)

//        val webAppInterface = WebAppInterface(this)
//        addJavascriptInterface(webAppInterface, webAppInterface.appBridgeJsName)

        addJavascriptInterface(MyWebAppInterface(context, asyncCallbacks), Config.JAVASCRIPT_BRIDGE_NAME)

        loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
    }
}