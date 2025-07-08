package com.taiwanlife.teamwalk.ui.main.webview

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.ui.common.model.DeviceInfoModel
import com.taiwanlife.teamwalk.ui.common.model.LoginInfoModel
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.getGson
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class MyWebAppInterface(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val webView: MyWebView,
    private val asyncCallbacks: AsyncCallbacks
) {

    companion object {
        const val CALLBACK_GET_GRAPHICAL_LOGIN = "graphicalLoginResolver"
        const val CALLBACK_BINDING_GOOGLE_HEALTH = "bindGoogleHealthConnectResolver"
        const val CALLBACK_BINDING_GARMIN_HEALTH = "bindGarminHealthResolver"
        const val CALLBACK_BINDING_FITBIT_HEALTH = "bindFitbitHealthResolver"
        const val CALLBACK_PUSH_MESSAGE_STATUS = "openNotificationResolver"
        const val CALLBACK_SYNC_HEALTH_DATA = "syncHealthDataResolver"
    }


    interface AsyncCallbacks {
        fun setGraphicalLogin(enable: String)
        fun bindingGoogleHealth(enable: String)
        fun bindingGarminHealth(enable: String)
        fun bindingFitbitHealth(enable: String)
        fun setPushMessageStatus(enable: String)
        fun syncHealthData()
    }

    private val sharedEventViewModel: SharedEventViewModel by inject(SharedEventViewModel::class.java)
    private var currentWaitingCallbackName = ""

    init {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                sharedEventViewModel.eventFlow.collect { (eventName, result) ->
                    Timber.d("eventName = $eventName, result = $result")
                    if (eventName.equals(EVENT_EXECUTE_JAVASCRIPT_CALLBACK) && currentWaitingCallbackName.isNotEmpty()) {
                        webView.post {
                            webView.evaluateJavascript("$currentWaitingCallbackName(${result})", null)

                            currentWaitingCallbackName = ""
                        }
                    }
                }
            }
        }
    }

    /**
     * 取得裝置相關資訊
     */
    @JavascriptInterface
    fun getDeviceInfo(): String {
        val deviceInfoModel = DeviceInfoModel(
            appUuid = SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, ""),
            deviceId = Utils.getDeviceId(context),
            pushId = SecuredPreferenceStoreManager.getString(Config.SP_FCM_TOKEN, "")
        )
        Toast.makeText(context, getGson().toJson(deviceInfoModel), Toast.LENGTH_SHORT).show()
        return getGson().toJson(deviceInfoModel)
    }

    /**
     * 取得登入JWT Token資訊
     */
    @JavascriptInterface
    fun getLoginInfo(): String {
        val loginInfoModel = LoginInfoModel(
            jwt = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT_TOKEN, "")
        )
        Toast.makeText(context, getGson().toJson(loginInfoModel), Toast.LENGTH_SHORT).show()
        return getGson().toJson(loginInfoModel)
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
    fun setGraphicalLogin(enable: String) {
        currentWaitingCallbackName = CALLBACK_GET_GRAPHICAL_LOGIN
        asyncCallbacks.setGraphicalLogin(enable)
    }

    /**
     * 綁定Google Health Connect
     */
    @JavascriptInterface
    fun bindingGoogleHealth(enable: String) {
        currentWaitingCallbackName = CALLBACK_BINDING_GOOGLE_HEALTH
        asyncCallbacks.bindingGoogleHealth(enable)
    }

    /**
     * 綁定Garmin Health
     */
    @JavascriptInterface
    fun bindingGarminHealth(enable: String) {
        currentWaitingCallbackName = CALLBACK_BINDING_GARMIN_HEALTH
        asyncCallbacks.bindingGarminHealth(enable)
    }

    /**
     * 綁定Fitbit Health
     */
    @JavascriptInterface
    fun bindingFitbitHealth(enable: String) {
        currentWaitingCallbackName = CALLBACK_BINDING_FITBIT_HEALTH
        asyncCallbacks.bindingFitbitHealth(enable)
    }

    /**
     * 開啟推播通知
     */
    @JavascriptInterface
    fun setPushMessageStatus(enable: String) {
        currentWaitingCallbackName = CALLBACK_PUSH_MESSAGE_STATUS
        asyncCallbacks.setPushMessageStatus(enable)
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
        currentWaitingCallbackName = CALLBACK_SYNC_HEALTH_DATA
        asyncCallbacks.syncHealthData()
    }
}