package com.taiwanlife.teamwalk.ui.main.webview

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.net.toUri
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.ui.main.webview.model.DeviceInfo
import com.taiwanlife.teamwalk.ui.main.webview.model.LoginInfo
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.getGson

class MyWebAppInterface(private val context: Context, private val asyncCallbacks: AsyncCallbacks) {

    interface AsyncCallbacks {
        fun setGraphicalLogin(enable: String, finished: () -> Unit)
        fun bindingGoogleHealth(enable: String, finished: () -> Unit)
        fun bindingGarminHealth(enable: String, finished: () -> Unit)
        fun bindingFitbitHealth(enable: String, finished: () -> Unit)
        fun setPushMessageStatus(enable: String, finished: () -> Unit)
        fun syncHealthData(finished: () -> Unit)

    }

    /**
     * 取得裝置相關資訊
     */
    @JavascriptInterface
    fun getDeviceInfo(): String {
        val deviceInfo = DeviceInfo(
            appUuid = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_FID, ""),
            deviceId = Utils.getDeviceId(context),
            pushId = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_FID, "")
        )
        Toast.makeText(context, getGson().toJson(deviceInfo), Toast.LENGTH_SHORT).show()
        return getGson().toJson(deviceInfo)
    }

    /**
     * 取得登入JWT Token資訊
     */
    @JavascriptInterface
    fun getLoginInfo(): String {
        val loginInfo = LoginInfo(
            jwt = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT_TOKEN, "")
        )
        Toast.makeText(context, getGson().toJson(loginInfo), Toast.LENGTH_SHORT).show()
        return getGson().toJson(loginInfo)
    }

    /**
     * 另開原生瀏覽器
     */
    @JavascriptInterface
    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "無法開啟網頁 url = $url", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 取得APP版本號
     */
    @JavascriptInterface
    fun getAppVersion(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version =
            if (!TextUtils.isEmpty(packageInfo.versionName)) packageInfo.versionName!! else ""

        Toast.makeText(context, version, Toast.LENGTH_SHORT).show()
        return version
    }

    /**
     * 設定圖形密碼登入
     */
    @JavascriptInterface
    fun setGraphicalLogin(enable: String): String {
        asyncCallbacks.setGraphicalLogin(enable) {

        }
        return ""
    }

    /**
     * 綁定Google Health Connect
     */
    @JavascriptInterface
    fun bindingGoogleHealth(enable: String): String {
        asyncCallbacks.bindingGoogleHealth(enable) {

        }
        return ""
    }

    /**
     * 綁定Garmin Health
     */
    @JavascriptInterface
    fun bindingGarminHealth(enable: String): String {
        asyncCallbacks.bindingGarminHealth(enable) {

        }
        return ""
    }

    /**
     * 綁定Fitbit Health
     */
    @JavascriptInterface
    fun bindingFitbitHealth(enable: String): String {
        asyncCallbacks.bindingFitbitHealth(enable) {

        }
        return ""
    }

    /**
     * 開啟推播通知
     */
    @JavascriptInterface
    fun setPushMessageStatus(enable: String): String {
        asyncCallbacks.setPushMessageStatus(enable) {

        }
        return "Y"
    }

    /**
     * 手機分享
     * 我在TeamWalk 的推薦碼是［ABC123］，快跟上我的腳步吧！
     *       TeamWalk APP下載連結
     *       ios: https://pros.is/3mx4g6
     *       Android: https://pros.is/3nbe55
     */
    @JavascriptInterface
    fun share(shareText: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(intent, "")
        context.startActivity(chooser)
    }

    /**
     * 同步健康數據
     */
    @JavascriptInterface
    fun syncHealthData() {
        asyncCallbacks.syncHealthData() {

        }
    }
}