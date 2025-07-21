package com.taiwanlife.teamwalk.ui.onboarding

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.databinding.ActivityConnectBinding
import com.taiwanlife.teamwalk.ui.common.FitbitViewModel
import com.taiwanlife.teamwalk.ui.common.GarminViewModel
import com.taiwanlife.teamwalk.ui.common.model.FitbitData
import com.taiwanlife.teamwalk.ui.common.model.GarminData
import com.taiwanlife.teamwalk.ui.onboarding.model.UserInfo
import com.taiwanlife.teamwalk.utils.BindingManager
import com.taiwanlife.teamwalk.utils.DeviceType
import com.taiwanlife.teamwalk.utils.DeviceType.FITBIT
import com.taiwanlife.teamwalk.utils.DeviceType.GARMIN
import com.taiwanlife.teamwalk.utils.DeviceType.HEALTH_CONNECT
import com.taiwanlife.teamwalk.utils.DeviceType.NONE
import com.taiwanlife.teamwalk.utils.HealthConnectHelper
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.getGson
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConnectActivity :
    OnBoardingActivity<ActivityConnectBinding>({ ActivityConnectBinding.inflate(it) }) {

    private val garminViewModel: GarminViewModel by viewModel()
    private val fitbitViewModel: FitbitViewModel by viewModel()
    private val healthConnectHelper = HealthConnectHelper(this, this)

    private lateinit var bindingManager: BindingManager

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onLastCreateBaseActivity(view, savedInstanceState)

        bindingManager = BindingManager(
            this,
            garminViewModel,
            fitbitViewModel,
            healthConnectHelper,
            {},
            ::bindingRemoved,
            ::bindNewDeviceSuccess
        )

        intent.getStringExtra(KEY_USER_INFO)?.let {
            userInfo = getGson().fromJson(it, UserInfo::class.java)
        }

        viewBinding.onboardingSkipButton.setOnClickListener {
            saveUserAndFinishAll()
        }
        viewBinding.onboardingNextButton.setOnClickListener {
            saveUserAndFinishAll()
        }
        setDialog()
        viewBinding.garminMask.setOnClickListener {
            if (viewBinding.onboardingCheckboxGarmin.isChecked) {
                // 有綁定 解除綁定流程
                bindingManager.removeDevice(GARMIN)
            } else {
                // 沒有綁定 開始綁定
                bindingManager.bindNewDevice(GARMIN)
            }
        }
        viewBinding.fitbitMask.setOnClickListener {
            if (viewBinding.onboardingCheckboxFitbit.isChecked) {
                // 有綁定 解除綁定流程
                bindingManager.removeDevice(FITBIT)
            } else {
                // 沒有綁定 開始綁定
                bindingManager.bindNewDevice(FITBIT)
            }
        }

        viewBinding.healthConnectMask.setOnClickListener {
            if (viewBinding.checkboxHealthConnect.isChecked) {
                bindingManager.removeDevice(HEALTH_CONNECT)
            } else {
                bindingManager.bindNewDevice(HEALTH_CONNECT)
            }
        }
        setCheckBoxAndNext()
    }

    private fun bindingRemoved(deviceType: DeviceType) {
        debugToast("${deviceType.displayName} removed")
        userInfo = userInfo.copy(
            bindingType = null,
            bindingToken = null,
        )

        setCheckBoxAndNext()
    }

    private fun bindNewDeviceSuccess(deviceType: DeviceType, data: String?) {
        debugToast("${deviceType.displayName} bind success")

        when (deviceType) {
            HEALTH_CONNECT -> {
                userInfo = userInfo.copy(
                    bindingType = deviceType.value,
                    bindingToken = null,
                )
            }

            GARMIN -> {
                if (!TextUtils.isEmpty(data)) {
                    val garminData = getGson().fromJson(data, GarminData::class.java)
                    if (!garminData.oauthToken.isNullOrEmpty()) {
                        userInfo = userInfo.copy(
                            bindingType = deviceType.value,
                            bindingToken = garminData.oauthToken,
                        )
                    }
                }
            }

            FITBIT -> {
                if (!TextUtils.isEmpty(data)) {
                    val fitbitData = getGson().fromJson(data, FitbitData::class.java)
                    if (!fitbitData.accessToken.isNullOrEmpty()) {
                        userInfo = userInfo.copy(
                            bindingType = deviceType.value,
                            bindingToken = fitbitData.accessToken,
                        )
                    }
                }
            }

            NONE -> {
                userInfo = userInfo.copy(
                    bindingType = null,
                    bindingToken = null,
                )
            }
        }

        setCheckBoxAndNext()
    }

    private fun setCheckBoxAndNext() {
        val currentDeviceType = bindingManager.getCurrentDeviceType()

        if(userInfo.bindingType.isNullOrEmpty()) {
            viewBinding.checkboxHealthConnect.isChecked = false
            viewBinding.onboardingCheckboxFitbit.isChecked = false
            viewBinding.onboardingCheckboxGarmin.isChecked = false
            viewBinding.onboardingCheckboxGoogleFit.isChecked = false
        } else {
            val deviceType = DeviceType.getDeviceTypeFromValue(userInfo.bindingType!!)
            viewBinding.checkboxHealthConnect.isChecked = false
            viewBinding.onboardingCheckboxFitbit.isChecked = false
            viewBinding.onboardingCheckboxGarmin.isChecked = false
            viewBinding.onboardingCheckboxGoogleFit.isChecked = false
            when(deviceType) {
                HEALTH_CONNECT -> {
                    viewBinding.checkboxHealthConnect.isChecked = true
                }
                GARMIN -> {
                    viewBinding.onboardingCheckboxGarmin.isChecked = true
                }
                FITBIT -> {
                    viewBinding.onboardingCheckboxFitbit.isChecked = true
                }
                NONE -> {
                }
            }
        }

        viewBinding.onboardingNextButton.text = getString(R.string.onboarding_connect_next)

        when (currentDeviceType) {
            HEALTH_CONNECT -> {
                viewBinding.checkboxHealthConnect.isChecked = true
            }

            GARMIN -> {
                viewBinding.onboardingCheckboxGarmin.isChecked = true
            }

            FITBIT -> {
                viewBinding.onboardingCheckboxFitbit.isChecked = true
            }

            NONE -> {
                viewBinding.onboardingNextButton.text = getString(R.string.onboarding_connect_skip)
            }
        }
    }

    private fun setDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.fragment_dialog, null)

        val infoText = dialogView.findViewById<TextView?>(R.id.onboarding_dialog_textContainer)
        infoText.text = Html.fromHtml(
            getResources().getString(R.string.onboard_connect_info),
            Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
        )

        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        dialog.setContentView(dialogView)

        viewBinding.onboardingImageViewInfo.setOnClickListener(View.OnClickListener { v: View? ->
//            dialog.show();
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.onboard_connect_message))
            builder.setIcon(R.mipmap.ic_launcher)
            builder.setCancelable(false)

            //设置正面按钮
            builder.setPositiveButton(
                getString(R.string.ok),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }
                })
            val alertDialog = builder.create()
            alertDialog.show()
        })

        val closeButton = dialogView.findViewById<Button?>(R.id.onboarding_dialog_close)
        closeButton.setOnClickListener(View.OnClickListener { v: View? ->
            dialog.dismiss()
        })
    }
}