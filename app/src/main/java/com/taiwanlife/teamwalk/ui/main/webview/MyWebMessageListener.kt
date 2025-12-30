package com.taiwanlife.teamwalk.ui.main.webview

import android.R.id.shareText
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.ui.common.model.DeviceInfoModel
import com.taiwanlife.teamwalk.ui.common.model.LoginInfoModel
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.getGson
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import kotlin.jvm.java
import androidx.core.text.isDigitsOnly

class MyWebMessageListener(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val asyncCallbacks: MyWebAppInterface.AsyncCallbacks
) {

    private val sharedEventViewModel: SharedEventViewModel by inject(SharedEventViewModel::class.java)
    private val allowedOrigins = setOf(
        context.getString(R.string.web_url),
        context.getString(R.string.api_url),
        context.getString(R.string.csso_url)
    )

    private var pendingReplyProxy: JavaScriptReplyProxy? = null

    fun init(webView: WebView) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            Timber.e("不支援 WebViewFeature.WEB_MESSAGE_LISTENER")
        } else {
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    sharedEventViewModel.eventFlow.collect { (eventName, result) ->
                        if (eventName == Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK) {
                            // 透過 WebMessage 直接回傳，不再需要 evaluateJavascript
                            pendingReplyProxy?.postMessage(result)
                            pendingReplyProxy = null
                        }
                    }
                }
            }

            WebViewCompat.addWebMessageListener(
                webView,
                "androidBridge", // JS 端的呼叫對象名稱
                allowedOrigins
            ) { view, message, sourceOrigin, isMainFrame, replyProxy ->
                val data = message.data ?: return@addWebMessageListener
                val command = try {
                    getGson().fromJson(data, WebCommand::class.java)
                } catch (e: Exception) {
                    Timber.e(e, "解析 WebMessage 失敗")
                    return@addWebMessageListener
                }

                handleCommand(command, replyProxy)
            }
        }
    }

    @SuppressLint("RequiresFeature")
    private fun handleCommand(command: WebCommand, replyProxy: JavaScriptReplyProxy) {
        when (command.action) {
            "getDeviceInfo" -> {
                val model = DeviceInfoModel(
                    appUuid = SecuredPreferenceStoreManager.getString(
                        Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID,
                        ""
                    ),
                    deviceId = Utils.getDeviceId(context),
                    pushId = SecuredPreferenceStoreManager.getString(Config.SP_FCM_IDENTIFIER, "")
                )
                replyProxy.postMessage(getGson().toJson(model))
            }

            "getLoginInfo" -> {
                val model = LoginInfoModel(
                    jwt = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_JWT, "")
                )
                replyProxy.postMessage(getGson().toJson(model))
            }

            "getAppVersion" -> {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                replyProxy.postMessage(packageInfo.versionName ?: "")
            }

            "openBrowser" -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, command.params?.toUri())
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.debugToast("無法開啟網頁")
                }
            }

            "share" -> {
                val shareContent = getGson().fromJson(command.params, ShareContentModel::class.java)
                if (shareContent.status == "TEXT") {
                    shareText(shareContent.message)
                } else {
                    Utils.shareBase64ImageSecure(context, shareContent.message)
                }
            }

            "updatePushCount" -> {
                if (command.params!= null && command.params.isDigitsOnly()) {
                    asyncCallbacks.updatePushCount(command.params.toInt())
                }
            }

            "saveDataToFile" -> {
                val saveModel = getGson().fromJson(command.params, SaveDataToFileModel::class.java)
                asyncCallbacks.saveDataToFile(saveModel.data, saveModel.fileName)
            }

            // 以下為需要透過 ViewModel 處理的非同步操作
            "setGraphicalLogin", "bindingGoogleHealth", "bindingGarminHealth",
            "bindingFitbitHealth", "setPushMessageStatus", "syncHealthData" -> {
                pendingReplyProxy = replyProxy
                when (command.action) {
                    "setGraphicalLogin" -> asyncCallbacks.setGraphicalLogin(command.params ?: "")
                    "bindingGoogleHealth" -> asyncCallbacks.bindingGoogleHealth(
                        command.params ?: ""
                    )

                    "bindingGarminHealth" -> asyncCallbacks.bindingGarminHealth(
                        command.params ?: ""
                    )

                    "bindingFitbitHealth" -> asyncCallbacks.bindingFitbitHealth(
                        command.params ?: ""
                    )

                    "setPushMessageStatus" -> asyncCallbacks.setPushMessageStatus(
                        command.params ?: ""
                    )

                    "syncHealthData" -> asyncCallbacks.syncHealthData()
                }
            }

            "logout" -> asyncCallbacks.logout()
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

    data class WebCommand(val action: String, val params: String?)
}