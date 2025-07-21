package com.taiwanlife.teamwalk.ui.onboarding

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.databinding.ActivityPromoteBinding
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import com.taiwanlife.teamwalk.ui.onboarding.model.UserInfo
import com.taiwanlife.teamwalk.utils.CustomTextWatcher
import com.taiwanlife.teamwalk.utils.DeviceType
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.toast

class PromoteActivity :
    OnBoardingActivity<ActivityPromoteBinding>({ ActivityPromoteBinding.inflate(it) }) {

    companion object {
        fun startPromoteActivity(context: Context, userInfoResponse: UserInfoResponse): Intent {
            val bindingType = if (userInfoResponse.bindingFibit == true) {
                DeviceType.FITBIT.value
            } else if (userInfoResponse.bindingGarmin == true) {
                DeviceType.GARMIN.value
            } else if (userInfoResponse.bindingAndroid == true) {
                DeviceType.HEALTH_CONNECT.value
            } else {
                null
            }
            val userInfo = UserInfo(
                referrerCode = userInfoResponse.referrerCode ?: "",
                nickname = userInfoResponse.getAvailableNickName(),
                bindingType = bindingType
            )

            val onboardingIntent = Intent(context, PromoteActivity::class.java)
            onboardingIntent.putExtra(KEY_USER_INFO, getGson().toJson(userInfo))

            return onboardingIntent
        }
    }

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onLastCreateBaseActivity(view, savedInstanceState)

        viewBinding.onboardingSkipButton.setOnClickListener {
            saveUserAndFinishAll()
        }
        setDialog()
        viewBinding.onboardingEditTextPromoteCode.setText(userInfo.referrerCode)
        viewBinding.onboardingEditTextPromoteCode.addTextChangedListener(CustomTextWatcher {
            val currentText = viewBinding.onboardingEditTextPromoteCode.text.toString().trim()
            if (TextUtils.isEmpty(currentText)) {
                toast(getString(R.string.onboard_promote_editText) + " " + getString(R.string.empty))
                userInfo = userInfo.copy(referrerCode = "")
            } else {
                userInfo = userInfo.copy(referrerCode = currentText)
            }
        })

        viewBinding.onboardingNextButton.setOnClickListener {
            if (!TextUtils.isEmpty(userInfo.referrerCode) && userInfo.referrerCode!!.length < 8) {
                toast(R.string.onboard_promote_msg)
                return@setOnClickListener
            }

            toAvatarActivity()
        }
    }

    private fun setDialog() {
        val dialogView: View = layoutInflater.inflate(R.layout.fragment_dialog, null)

        val infoText = dialogView.findViewById<TextView?>(R.id.onboarding_dialog_textContainer)
        infoText.text = Html.fromHtml(
            getString(R.string.onboard_promote_info),
            Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
        )

        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        dialog.setContentView(dialogView)

        viewBinding.onboardingImageViewInfo.setOnClickListener {
            dialog.show()
        }

        val closeButton = dialogView.findViewById<Button?>(R.id.onboarding_dialog_close)
        closeButton.setOnClickListener(View.OnClickListener { v: View? ->
            dialog.dismiss()
        })
    }
}