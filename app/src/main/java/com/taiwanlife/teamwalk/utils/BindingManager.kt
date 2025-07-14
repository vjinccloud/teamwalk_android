package com.taiwanlife.teamwalk.utils

import android.content.Intent
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.remote.GarminHelper
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.ui.common.FitbitViewModel
import com.taiwanlife.teamwalk.ui.common.GarminViewModel
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.ui.common.model.FitbitData
import com.taiwanlife.teamwalk.ui.common.model.GarminData
import com.taiwanlife.teamwalk.utils.DeviceType.FITBIT
import com.taiwanlife.teamwalk.utils.DeviceType.GARMIN
import com.taiwanlife.teamwalk.utils.DeviceType.HEALTH_CONNECT
import com.taiwanlife.teamwalk.utils.DeviceType.NONE
import kotlinx.coroutines.launch
import java.util.Locale

enum class DeviceType(val value: String, val displayName: String) {
    HEALTH_CONNECT("HEALTH_CONNECT", "Health Connect"),
    GARMIN("GARMIN", "Garmin"),
    FITBIT("FITBIT", "Fitbit"),
    NONE("NONE", "");

    companion object {
        fun getDeviceTypeFromValue(value: String): DeviceType {
            val type = DeviceType.entries.find { it.value == value }
            return type ?: NONE
        }
    }
}

class BindingManager(
    private val baseActivity: BaseActivity<*>,
    private val garminViewModel: GarminViewModel,
    private val fitbitViewModel: FitbitViewModel,
    private val healthConnectHelper: HealthConnectHelper,
    private val sameDeviceCallback: (deviceType: DeviceType) -> Unit = {},
    private val bindingRemovedCallback: (deviceType: DeviceType) -> Unit,
    private val bindingNewDeviceSuccessCallback: (newDeviceType: DeviceType, data: String?) -> Unit
) {
    private val sharedEventViewModel: SharedEventViewModel =
        baseActivity.getSharedEventViewModelInstance()

    init {
        // 這裡接網頁的Redirection成功後的處理
        baseActivity.lifecycleScope.launch {
            baseActivity.repeatOnLifecycle(Lifecycle.State.CREATED) {
                sharedEventViewModel.eventFlow.collect { (eventName, result) ->
                    onReceivedEvent(eventName, result)
                }
            }
        }
    }

    fun getCurrentDeviceType(): DeviceType {
        val deviceTypeString =
            SecuredPreferenceStoreManager.getString(Config.SP_BIND_CURRENT_DEVICE, "")
        return DeviceType.getDeviceTypeFromValue(deviceTypeString)
    }

    /**
     * 開始綁定裝置流程
     */
    fun bindNewDevice(deviceType: DeviceType) {
        val currentDeviceType = getCurrentDeviceType()
        if (deviceType == currentDeviceType) {
            // 現在想要綁定的裝置已經是目前的裝置了 不做動作
            sameDeviceCallback(currentDeviceType)
            return
        }

        // 根據不同的裝置啟動不同的綁定流程 注意點是如果現有綁訂其他裝置 要先解除所有裝置在綁定
        if (currentDeviceType != NONE) {
            CommonDialog(baseActivity).apply {
                twoButtonInit(
                    title = context.getString(R.string.binding_change_device_title),
                    body = String.format(
                        Locale.getDefault(),
                        context.getString(R.string.binding_change_device_message),
                        currentDeviceType.displayName
                    ),
                    showButtons = true,
                    canceledOnTouchOutside = true,
                    image = R.drawable.alert_1,
                    positiveText = context.getString(R.string.binding_change_device_confirm),
                    positiveOnClick = {
                        removeAllDevice()

                        // 移除裝置之後呼叫callback去處理刪除的邏輯
                        bindingRemovedCallback(currentDeviceType)

                        // 移除現有裝置之後 開始綁定新裝置
                        startBindingProcess(deviceType)
                    },
                    negativeText = context.getString(R.string.binding_change_device_cancel),
                    negativeOnClick = {}
                )
            }.show()
        } else {
            startBindingProcess(deviceType)
        }
    }


    /**
     * 解除現在的綁定裝置 基本上就是呼叫解除所有裝置
     */
    fun removeDevice(deviceType: DeviceType) {
        removeAllDevice()

        // 如果是Health Connect 需要多問是否要去關閉權限
        if (deviceType == HEALTH_CONNECT) {
            CommonDialog(baseActivity).apply {
                twoButtonInit(
                    title = baseActivity.getString(R.string.binding_change_health_connect_title),
                    body = baseActivity.getString(R.string.binding_change_health_connect_message),
                    image = R.drawable.alert_1,
                    positiveText = baseActivity.getString(R.string.binding_change_health_connect_confirm),
                    positiveOnClick = {
                        baseActivity.startActivity(
                            healthConnectHelper.createManagePermissionsIntent(baseActivity)
                        )
                    },
                    negativeText = baseActivity.getString(R.string.binding_change_health_connect_cancel),
                    negativeOnClick = {},
                    showButtons = true,
                    canceledOnTouchOutside = true
                )
            }.show()
        }


        // 成功後結果
        bindingRemovedCallback(deviceType)
    }

    /**
     * 移除所有裝置
     */
    fun removeAllDevice() {
        // 這邊應該會有一隻API device/deleteAll

        // 模擬成功後結果
        SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_BIND_CURRENT_DEVICE, NONE.value)
    }

    /**
     * 裝置綁定成功
     */
    fun bindProcessFinished(deviceType: DeviceType, data: String?) {
        // 這邊應該會有一隻API device

        // 模擬成功之後的結果
        SecuredPreferenceStoreManager.simpleEditAndApply(
            Config.SP_BIND_CURRENT_DEVICE,
            deviceType.value
        )

        bindingNewDeviceSuccessCallback(deviceType, data)
    }

    private fun startBindingProcess(deviceType: DeviceType) {
        when (deviceType) {
            HEALTH_CONNECT -> startHealthConnectProcess()
            GARMIN -> startGarminProcess()
            FITBIT -> startFitbitProcess()
            NONE -> {
                // 結束流程
                SecuredPreferenceStoreManager.simpleEditAndApply(
                    Config.SP_BIND_CURRENT_DEVICE,
                    deviceType.value
                )
            }
        }
    }

    private fun startHealthConnectProcess() {
        if (!healthConnectHelper.availableStatusFlow()) return

        healthConnectHelper.requestPermissionFlow {
            if (!(!healthConnectHelper.availableStatusFlow() || !healthConnectHelper.checkPermissions())) {
                // 拿到所有我們需要的權限了 呼叫綁定完成API
                bindProcessFinished(HEALTH_CONNECT, null)
            }
        }
    }

    private fun startGarminProcess() {
        val authorization = GarminHelper.getGarminAuthorizationForAuthCode()
        baseActivity.observeOnLifeCycle(
            garminViewModel.getAuthCodeFlow.sharedFlow,
            unSubscribeOnComplete = true,
            onError = {
                baseActivity.toast(R.string.onboard_connect_fail)
            }) { response ->
            // 處理成功結果
            val responseString = response.string()
            if (!TextUtils.isEmpty(responseString)) {
                GarminHelper.parseGetAuthCodeString(responseString, getTsGarminCallback = {
                    val garminData = GarminData(tsGarmin = it)
                    SecuredPreferenceStoreManager.simpleEditAndApply(
                        Config.SP_BIND_GARMIN,
                        getGson().toJson(garminData)
                    )
                }, showFailedToast = {
                    baseActivity.toast(R.string.onboard_connect_fail)
                }, getUrlCallback = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    baseActivity.startActivity(intent)
                })
            } else {
                baseActivity.toast(R.string.onboard_connect_fail)
            }
        }
        garminViewModel.getGarminAuthCode(authorization)
    }

    private fun startFitbitProcess() {
        baseActivity.startActivity(fitbitViewModel.getUrlIntent())
    }

    private fun onReceivedEvent(eventName: String?, result: String) {
        if (eventName == Config.EVENT_GARMIN_CONNECT_DONE) {
            val garminData = getGson().fromJson(result, GarminData::class.java)
            if (garminData.oauthToken != null && garminData.oauthTokenSecret != null) {

                bindProcessFinished(GARMIN, result)
            }
        } else if (eventName == Config.EVENT_FITBIT_CONNECT_DONE) {
            val fitbitData = getGson().fromJson(result, FitbitData::class.java)

            if (fitbitData.accessToken != null && fitbitData.refreshToken != null) {

                bindProcessFinished(FITBIT, result)
            }
        }
    }
}