package com.taiwanlife.teamwalk.ui.main.webview

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.JavascriptInterface
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.ui.common.model.DeviceInfoModel
import com.taiwanlife.teamwalk.ui.common.model.LoginInfoModel
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.quoteJS
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

// 會被Javascript呼叫 Android端unuse 故忽略警告
@Suppress("unused")
class MyWebAppInterface(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val webView: MyWebView,
    private val asyncCallbacks: AsyncCallbacks
) {

    companion object {
        const val CALLBACK_DEVICE_INFO_RESOLVER = "deviceInfoResolver"
        const val CALLBACK_JWT_TOKEN_RESOLVER = "jwtTokenResolver"
        const val CALLBACK_APP_VERSION_RESOLVER = "appVersionResolver"
        const val CALLBACK_GRAPHICAL_LOGIN_RESOLVER = "graphicalLoginResolver"
        const val CALLBACK_BIND_GOOGLE_HEALTH_CONNECT_RESOLVER = "bindGoogleHealthConnectResolver"
        const val CALLBACK_BIND_APPLE_IOS_HEALTH_RESOLVER = "bindAppleiOSHealthResolver"
        const val CALLBACK_BIND_GARMIN_HEALTH_RESOLVER = "bindGarminHealthResolver"
        const val CALLBACK_BIND_FITBIT_HEALTH_RESOLVER = "bindFitbitHealthResolver"
        const val CALLBACK_OPEN_NOTIFICATION_RESOLVER = "openNotificationResolver"
        const val CALLBACK_SYNC_HEALTH_DATA_RESOLVER = "syncHealthDataResolver"
    }


    interface AsyncCallbacks {
        fun setGraphicalLogin(enable: String)
        fun bindingGoogleHealth(enable: String)
        fun bindingGarminHealth(enable: String)
        fun bindingFitbitHealth(enable: String)
        fun setPushMessageStatus(enable: String)
        fun syncHealthData()
        fun updatePushCount(notifyCount: Int)
        fun logout()
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
                            var test = ""
                            webView.evaluateJavascript(
                                "$currentWaitingCallbackName$test(${result})",
                                null
                            )

                            context.debugToast("透過${currentWaitingCallbackName}回傳結果 - $result")
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
    fun getDeviceInfo() {
        val deviceInfoModel = DeviceInfoModel(
            appUuid = SecuredPreferenceStoreManager.getString(
                Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID,
                ""
            ),
            deviceId = Utils.getDeviceId(context),
            pushId = SecuredPreferenceStoreManager.getString(Config.SP_FCM_TOKEN, "")
        )

        currentWaitingCallbackName = CALLBACK_DEVICE_INFO_RESOLVER
        sharedEventViewModel.postEvent(
            getGson().toJson(deviceInfoModel).quoteJS(),
            EVENT_EXECUTE_JAVASCRIPT_CALLBACK
        )
    }

    /**
     * 取得登入JWT Token資訊
     */
    @JavascriptInterface
    fun getLoginInfo() {
        val loginInfoModel = LoginInfoModel(
            jwt = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT_TOKEN, "")
        )

        Timber.d(getGson().toJson(loginInfoModel))

        currentWaitingCallbackName = CALLBACK_JWT_TOKEN_RESOLVER
        sharedEventViewModel.postEvent(
            getGson().toJson(loginInfoModel).quoteJS(),
            EVENT_EXECUTE_JAVASCRIPT_CALLBACK
        )
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
            context.debugToast("無法開啟網頁 url = $url")
            e.printStackTrace()
        }
    }

    /**
     * 取得APP版本號
     */
    @JavascriptInterface
    fun getAppVersion() {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version =
            if (!TextUtils.isEmpty(packageInfo.versionName)) packageInfo.versionName!! else ""

        currentWaitingCallbackName = CALLBACK_APP_VERSION_RESOLVER
        sharedEventViewModel.postEvent(version.quoteJS(), EVENT_EXECUTE_JAVASCRIPT_CALLBACK)
    }

    /**
     * 設定圖形密碼登入
     */
    @JavascriptInterface
    fun setGraphicalLogin(enable: String) {
        currentWaitingCallbackName = CALLBACK_GRAPHICAL_LOGIN_RESOLVER
        asyncCallbacks.setGraphicalLogin(enable)
    }

    /**
     * 綁定Google Health Connect
     */
    @JavascriptInterface
    fun bindingGoogleHealth(enable: String) {
        currentWaitingCallbackName = CALLBACK_BIND_GOOGLE_HEALTH_CONNECT_RESOLVER
        asyncCallbacks.bindingGoogleHealth(enable)
    }

    /**
     * 綁定Garmin Health
     */
    @JavascriptInterface
    fun bindingGarminHealth(enable: String) {
        currentWaitingCallbackName = CALLBACK_BIND_GARMIN_HEALTH_RESOLVER
        asyncCallbacks.bindingGarminHealth(enable)
    }

    /**
     * 綁定Fitbit Health
     */
    @JavascriptInterface
    fun bindingFitbitHealth(enable: String) {
        currentWaitingCallbackName = CALLBACK_BIND_FITBIT_HEALTH_RESOLVER
        asyncCallbacks.bindingFitbitHealth(enable)
    }

    /**
     * 開啟推播通知
     */
    @JavascriptInterface
    fun setPushMessageStatus(enable: String) {
        currentWaitingCallbackName = CALLBACK_OPEN_NOTIFICATION_RESOLVER
        asyncCallbacks.setPushMessageStatus(enable)
    }

    /**
     * 手機分享
     * {
     *  “content-type: “TEXT | IMAGE”,
     *  “msg”: "我在TeamWalk 的推薦碼是［ABC123］，快跟上我的腳步吧！
     *  TeamWalk APP下載連結
     *  ios: https://pros.is/3mx4g6
     *  Android: https://pros.is/3nbe55" ,
     *
     * }
     */
    @JavascriptInterface
    fun share(shareText: String) {
        try {
            val json = if(shareText != "undefined") shareText else Gson().toJson(testModel)
            val shareContentModel = getGson().fromJson(json, ShareContentModel::class.java)
            when(shareContentModel.status) {
                "TEXT" -> {
                    shareText(shareContentModel.message)
                }
                "IMAGE" -> {
                    Utils.shareBase64ImageSecure(context, shareContentModel.message)
                }
                else -> {
                    context.debugToast("content-type  格式錯誤 使用舊分享")
                    shareText(shareText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.debugToast("分享內容格式錯誤 使用舊分享")
            shareText(shareText)
        }

    }

    fun shareText(message: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
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
        currentWaitingCallbackName = CALLBACK_SYNC_HEALTH_DATA_RESOLVER
        asyncCallbacks.syncHealthData()
    }

    /**
     * app推播未讀紅點數
     */
    @JavascriptInterface
    fun updatePushCount(notifyCountString: String) {
        context.debugToast("收到未讀 - $notifyCountString")
        if(TextUtils.isDigitsOnly(notifyCountString)) {
            val notifyCount = notifyCountString.toInt()
            asyncCallbacks.updatePushCount(notifyCount)
        }
    }

    /**
     * web登出回到app登入頁
     */
    @JavascriptInterface
    fun logout() {
        asyncCallbacks.logout()
    }
}

data class ShareContentModel(
    @SerializedName("status")
    val status: String,
    @SerializedName("msg")
    val message: String
)

val testModel = ShareContentModel(
    "IMAGE",
    "data:image/gif;base64,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"
)