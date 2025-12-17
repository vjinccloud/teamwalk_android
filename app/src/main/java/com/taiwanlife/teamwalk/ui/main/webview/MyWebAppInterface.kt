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

        fun saveDataToFile(data: String, fileName: String)
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
            jwt = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT, "")
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
            if (shareText == "undefined") {
                context.debugToast("分享字串 $shareText")
                return
            }

            val shareContentModel = getGson().fromJson(shareText, ShareContentModel::class.java)
            when (shareContentModel.status) {
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
        if (TextUtils.isDigitsOnly(notifyCountString)) {
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

    @JavascriptInterface
    fun saveDataToFile(result: String) {
        try {
            val saveDataToFileModel = getGson().fromJson(result, SaveDataToFileModel::class.java)
            asyncCallbacks.saveDataToFile(saveDataToFileModel.data, saveDataToFileModel.fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            context.debugToast("解析 saveDataToFile 錯誤 - $result")
        }
    }
}

data class ShareContentModel(
    @SerializedName("status")
    val status: String,
    @SerializedName("msg")
    val message: String
)

data class SaveDataToFileModel(
    @SerializedName("data")
    val data: String,
    @SerializedName("fileName")
    val fileName: String
)