package com.taiwanlife.teamwalk.ui.main.old_logic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.java_utils.FortifyUtil
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager.simpleEditAndApply
import timber.log.Timber
import java.net.URLDecoder

class MainWebViewClient(
    private val context: Context,
    private val webView: WebView,
    private val mainWebViewJavaCallback: MainWebViewJava.MainWebViewJavaCallback
) : WebViewClient() {

    private val environmentConfig = EnvironmentManager.getEnvironmentConfig()
    private val env = BuildConfig.BUILD_TYPE

    /**
     * @param view
     * @param request
     * @return
     */
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        //Timber.d("shouldOverrideUrlLoading:"+request.getUrl().toString());
        if (request.getUrl().equals(environmentConfig.webUrl) && view.getTitle() != "Teamwalk") {
            webView.clearCache(true)
        }
        if (request.getUrl().toString().startsWith("tel:")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()))
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            context.startActivity(intent)
        } else {
            val cssoURL = Uri.parse(environmentConfig.cssoUrl)
            if (TextUtils.equals(cssoURL.getHost(), request.getUrl().getHost())) {
                if (TextUtils.equals("/csso/mobileIndex", request.getUrl().getPath())) {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + env + "://login"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }

                return false
            }

            val tcavURL = Uri.parse(environmentConfig.tcavUrl)
            if (TextUtils.equals(tcavURL.getHost(), request.getUrl().getHost())) {
                return false
            }

            val webURL = Uri.parse(environmentConfig.webUrl)
            if (TextUtils.equals(webURL.getHost(), request.getUrl().getHost())) {
                return false
            }

            val taiwanlife_member_uri = Uri.parse(environmentConfig.taiwanlifeMemberUrl)
            if (TextUtils.equals(taiwanlife_member_uri.getHost(), request.getUrl().getHost())) {
                var taiwanlife_member_str: String? = environmentConfig.taiwanlifeMemberUrl
                var request_str: String? = request.getUrl().toString()
                try {
                    taiwanlife_member_str = URLDecoder.decode(environmentConfig.taiwanlifeMemberUrl)
                    request_str = URLDecoder.decode(request.getUrl().toString())
                } catch (e: Exception) {
                    taiwanlife_member_str = environmentConfig.taiwanlifeMemberUrl
                    request_str = request.getUrl().toString()
                }
                if (TextUtils.equals(taiwanlife_member_str, request_str)) {
                    webView.loadUrl(environmentConfig.webUrl + "my/preferences")
                    Timber.i("shouldOverrideUrlLoading: return my/preferences for www.taiwanlife.com")
                    mainWebViewJavaCallback.showPwErrorDialog()
                    return true
                }
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()))
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            context.startActivity(intent)
        }
        return true
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        //            Log.i("LOG TIME webview url: " + url , Utilities.getDateNow());
        mainWebViewJavaCallback.enableLoading(true)
        //            loadingIndicator.setVisibility(View.VISIBLE);
        if (url.startsWith(environmentConfig.cssoUrl)) {
            mainWebViewJavaCallback.enableLoadingText(true, Color.BLACK)
            //                loadingText.setVisibility(View.VISIBLE);
//                loadingText.setTextColor(Color.BLACK);
        } else if (url.startsWith("teamwalk")) {
            val parse = Uri.parse(url)
            val host = parse.getHost()
            var actionUrl = ""
            val suffic = "teamwalk" + env
            when (host) {
                "home" -> actionUrl = suffic + "://home"
                "login" -> actionUrl = suffic + "://login"
                "loginsuccess" -> actionUrl =
                    suffic + "://loginsuccess?ticket=" + parse.getQueryParameter("ticket")

                "loginfailure" -> actionUrl = suffic + "://loginfailure"
                "userinfo" -> actionUrl = suffic + "://userinfo"
                "webconnect" -> actionUrl = suffic + "://webconnect"
            }
            if (actionUrl != "") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl))
                intent.putExtra("pid", mainWebViewJavaCallback.getPid())
                view.stopLoading()
                context.startActivity(intent)
                if (url.contains("loginsuccess")) {
                    webView.loadUrl("about:blank")
                }
            } else {
                mainWebViewJavaCallback.toLogin()
            }
        } else if (url == environmentConfig.webUrl) {
//                loadingText.setVisibility(View.VISIBLE);
//                loadingText.setTextColor(Color.GRAY);
            mainWebViewJavaCallback.enableLoadingText(true, Color.GRAY)
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        Timber.d("Current URL = " + url)
        if (view.getUrl() == environmentConfig.webUrl && view.getTitle() != "Teamwalk") {
//                clearCache = true;
            mainWebViewJavaCallback.setClearCache(true)
        }
        val csso_url: String = environmentConfig.cssoUrl
        val web_url: String = environmentConfig.webUrl

        if (csso_url.length > 0 && web_url.length > 0) {
            val cookies = CookieManager.getInstance().getCookie(csso_url + "login")
            if (!TextUtils.isEmpty(cookies)) {
                val cookieStringArray =
                    cookies!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var CASTGC_value: String? = null
                for (cookie in cookieStringArray) {
                    if (cookie.contains("CASTGC")) {
                        CASTGC_value = ""
                        val cookieArray = cookie.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        if (cookieArray.size < 2) continue
                        val cookieValue = cookieArray[1]
                        CASTGC_value = cookieValue
                        simpleEditAndApply(Config.PREF_LOGIN_CASTGC, cookieValue)
                        break
                    }
                }

                //Log.i("Cookies:", "onPageFinished setCookie:url="+url);
                var loginsuccessStr = "teamwalkuat://loginsuccess"
                if (BuildConfig.BUILD_TYPE.compareTo(
                        "release",
                        ignoreCase = true
                    ) == 0
                ) loginsuccessStr = "teamwalk://loginsuccess"
                if (url.indexOf(loginsuccessStr) == 0 && CASTGC_value != null) {
                    val newCookies = "CASTGC=" + CASTGC_value
                    CookieManager.getInstance().setCookie(web_url, newCookies)
                    CookieManager.getInstance().flush()
                    //Log.i("Cookies:", "onPageFinished setCookie:cookies=" + cookies);
                    //Log.i("Cookies:", "onPageFinished loginsuccess setCookie:newCookies=" + newCookies);
                } else {
                    val newCookies = FortifyUtil.filterCookiesForFortify(cookies)
                    if (newCookies.length > 0) {
                        CookieManager.getInstance().setCookie(web_url, newCookies)
                        CookieManager.getInstance().flush()
                        //Log.i("Cookies:", "onPageFinished setCookie:cookies=" + cookies);
                        //Log.i("Cookies:", "onPageFinished setCookie:newCookies=" + newCookies);
                    }
                }

                //                CookieManager.getInstance().setCookie(getString(R.string.web_url), cookies);
                //                CookieManager.getInstance().flush();
            }
        }

        mainWebViewJavaCallback.enableLoading(false)
        mainWebViewJavaCallback.enableLoadingText(false, Color.BLACK)
        //            loadingIndicator.setVisibility(View.GONE);
//            loadingText.setVisibility(View.GONE);
    } //        @Override
    //        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    //            super.onReceivedSslError(view, handler, error);
    //            if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("sit")) {
    //                handler.proceed();
    //            }
    //        }
}