package com.taiwanlife.teamwalk.ui.main.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.ui.common.model.CastGCModel
import com.taiwanlife.teamwalk.ui.common.model.DeviceInfoModel
import com.taiwanlife.teamwalk.ui.common.model.LoginInfoModel
import com.taiwanlife.teamwalk.ui.common.model.SaveDataToFileModel
import com.taiwanlife.teamwalk.ui.common.model.ShareContentModel
import com.taiwanlife.teamwalk.ui.common.model.WebCommand
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_APP_VERSION_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_BIND_FITBIT_HEALTH_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_BIND_GARMIN_HEALTH_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_BIND_GOOGLE_HEALTH_CONNECT_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_CASTGC_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_DEVICE_INFO_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_GRAPHICAL_LOGIN_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_JWT_TOKEN_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_OPEN_NOTIFICATION_RESOLVER
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView.Companion.CALLBACK_SYNC_HEALTH_DATA_RESOLVER
import com.taiwanlife.teamwalk.utils.AppUuidManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.quoteJS
import com.taiwanlife.teamwalk.utils.toOrigin
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class MyWebMessageListener(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val asyncCallbacks: AsyncCallbacks
) {

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

        // 0003016: Web 端載入完成通知，App 端用來取消 timeout watcher
        fun webviewFinished()
    }

    private val sharedEventViewModel: SharedEventViewModel by inject(SharedEventViewModel::class.java)
    private var currentWaitingCallbackName = ""
    private val allowedOrigins = setOf(
        context.getString(R.string.web_url).toUri().toOrigin(),
        context.getString(R.string.api_url).toUri().toOrigin(),
        context.getString(R.string.csso_url).toUri().toOrigin(),
    )

    //    private var pendingReplyProxy: JavaScriptReplyProxy? = null
    private lateinit var webView: WebView

    fun init(webView: WebView) {
        this.webView = webView

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                sharedEventViewModel.eventFlow.collect { (eventName, result) ->
                    Timber.d("eventName = $eventName, result = $result")
                    if (eventName.equals(EVENT_EXECUTE_JAVASCRIPT_CALLBACK) && currentWaitingCallbackName.isNotEmpty()) {
                        // 印出要回給 webview 的內容（callback 名稱 + value）
                        val callbackName = currentWaitingCallbackName
                        Timber.d("JS-BRIDGE-RETURN → callback=$callbackName, value=$result")
                        webView.post {
                            val js = "$callbackName(${result})"
                            webView.evaluateJavascript(js) { jsResult ->
                                Timber.d("JS-BRIDGE-RETURN done callback=$callbackName, jsResult=$jsResult")
                            }

                            context.debugToast("透過${callbackName}回傳結果 - $result")
                            currentWaitingCallbackName = ""
                        }
                    }
                }
            }
        }

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            Timber.e("不支援 WebViewFeature.WEB_MESSAGE_LISTENER")
        } else {
//            lifecycleOwner.lifecycleScope.launch {
//                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
//                    sharedEventViewModel.eventFlow.collect { (eventName, result) ->
//                        if (eventName == Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK) {
//                            // 透過 WebMessage 直接回傳
//                            pendingReplyProxy?.postMessage(result)
//                            pendingReplyProxy = null
//                        }
//                    }
//                }
//            }

            WebViewCompat.addWebMessageListener(
                webView,
                Config.JAVASCRIPT_BRIDGE_NAME,
                allowedOrigins
            ) { view, message, sourceOrigin, isMainFrame, replyProxy ->
                val data = message.data ?: return@addWebMessageListener
                try {
                    val command = getGson().fromJson(data, WebCommand::class.java)
                    handleCommand(command, replyProxy)
                } catch (e: Exception) {
                    Timber.e(e, "解析 WebMessage 失敗")
                }
            }
        }
    }

    @SuppressLint("RequiresFeature")
    private fun handleCommand(command: WebCommand, replyProxy: JavaScriptReplyProxy) {
        // 全部 bridge call 都印出來方便排查 SPA 到底有沒有呼叫到 native
        Timber.d("JS-BRIDGE-CALL action=${command.action}, status=${command.status}, url=${command.url}")

        // 0003016: WebviewFinished 大小寫不敏感比對（Web 規格大寫 W、Android 命名慣例小寫 w）
        if (command.action.equals("webviewFinished", ignoreCase = true)) {
            asyncCallbacks.webviewFinished()
            return
        }

        when (command.action) {
            "getDeviceInfo" -> {
                val deviceInfoModel = DeviceInfoModel(
                    appUuid = AppUuidManager.getOrCreate(),
                    deviceId = Utils.getDeviceId(context),
                    pushId = SecuredPreferenceStoreManager.getString(Config.SP_FCM_IDENTIFIER, "")
                )
//                replyProxy.postMessage(getGson().toJson(deviceInfoModel))
                currentWaitingCallbackName = CALLBACK_DEVICE_INFO_RESOLVER
                sharedEventViewModel.postEvent(
                    getGson().toJson(deviceInfoModel).quoteJS(),
                    EVENT_EXECUTE_JAVASCRIPT_CALLBACK
                )
            }

            "getLoginInfo" -> {
                val loginInfoModel = LoginInfoModel(
                    jwt = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT, "")
                )
//                replyProxy.postMessage(getGson().toJson(model))
                currentWaitingCallbackName = CALLBACK_JWT_TOKEN_RESOLVER
                sharedEventViewModel.postEvent(
                    getGson().toJson(loginInfoModel).quoteJS(),
                    EVENT_EXECUTE_JAVASCRIPT_CALLBACK
                )
            }

            "getAppVersion" -> {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version =
                    if (!TextUtils.isEmpty(packageInfo.versionName)) packageInfo.versionName!! else ""
//                replyProxy.postMessage(version ?: "")

                currentWaitingCallbackName = CALLBACK_APP_VERSION_RESOLVER
                sharedEventViewModel.postEvent(version.quoteJS(), EVENT_EXECUTE_JAVASCRIPT_CALLBACK)
            }

            "openBrowser" -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, command.url?.toUri())
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.debugToast("無法開啟網頁")
                    e.printStackTrace()
                }
            }

            "share" -> {
                val shareContent = getGson().fromJson(command.status, ShareContentModel::class.java)
                if (shareContent.status == "TEXT") {
                    shareText(shareContent.message)
                } else {
                    Utils.shareBase64ImageSecure(context, shareContent.message)
                }
            }

            "updatePushCount" -> {
                if (command.status != null && command.status.isDigitsOnly()) {
                    asyncCallbacks.updatePushCount(command.status.toInt())
                }
            }

            "saveDataToFile" -> {
                val saveModel = getGson().fromJson(command.status, SaveDataToFileModel::class.java)
                asyncCallbacks.saveDataToFile(saveModel.data, saveModel.fileName)
            }

            // 以下為需要透過 ViewModel 處理的非同步操作
            "setGraphicalLogin", "bindingGoogleHealth", "bindingGarminHealth",
            "bindingFitbitHealth", "setPushMessageStatus", "syncHealthData" -> {
//                pendingReplyProxy = replyProxy
                when (command.action) {
                    "setGraphicalLogin" -> {
                        currentWaitingCallbackName = CALLBACK_GRAPHICAL_LOGIN_RESOLVER
                        asyncCallbacks.setGraphicalLogin(command.status ?: "")
                    }

                    "bindingGoogleHealth" -> {
                        currentWaitingCallbackName = CALLBACK_BIND_GOOGLE_HEALTH_CONNECT_RESOLVER
                        asyncCallbacks.bindingGoogleHealth(
                            command.status ?: "Y"
                        )
                    }

                    "bindingGarminHealth" -> {
                        currentWaitingCallbackName = CALLBACK_BIND_GARMIN_HEALTH_RESOLVER
                        asyncCallbacks.bindingGarminHealth(
                            command.status ?: "Y"
                        )
                    }

                    "bindingFitbitHealth" -> {
                        currentWaitingCallbackName = CALLBACK_BIND_FITBIT_HEALTH_RESOLVER
                        asyncCallbacks.bindingFitbitHealth(
                            command.status ?: "Y"
                        )
                    }

                    "setPushMessageStatus" -> {
                        currentWaitingCallbackName = CALLBACK_OPEN_NOTIFICATION_RESOLVER
                        asyncCallbacks.setPushMessageStatus(
                            command.status ?: "Y"
                        )
                    }

                    "syncHealthData" -> {
                        currentWaitingCallbackName = CALLBACK_SYNC_HEALTH_DATA_RESOLVER
                        asyncCallbacks.syncHealthData()
                    }
                }
            }

            "logout" -> asyncCallbacks.logout()

            "getCastgc" -> {
                val castGCModel = CastGCModel(
                    castgc = SecuredPreferenceStoreManager.getString(Config.SP_CASTGC, "")
                )
//                replyProxy.postMessage(getGson().toJson(model))
                currentWaitingCallbackName = CALLBACK_CASTGC_RESOLVER
                sharedEventViewModel.postEvent(
                    getGson().toJson(castGCModel).quoteJS(),
                    EVENT_EXECUTE_JAVASCRIPT_CALLBACK
                )
            }
        }
    }

    private fun shareText(message: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, ""))
    }
}