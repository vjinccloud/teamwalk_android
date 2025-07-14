package com.taiwanlife.teamwalk.ui.pattern

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityPatternSetupBinding
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast

class PatternSetupActivity :
    BaseActivity<ActivityPatternSetupBinding>({ ActivityPatternSetupBinding.inflate(it) }) {

    companion object {
        const val KEY_IS_GRAPHICAL_LOGIN_SET = "KEY_IS_GRAPHICAL_LOGIN_SET"
        const val KEY_IS_GRAPHICAL_LOGIN_CHANGED = "KEY_IS_GRAPHICAL_LOGIN_CHANGED"
    }

    // 使用者是否再確認Pattern與第一次相符
    private var isConfirm: Boolean = false
    private var originalPattern: String? = null

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {

        viewBinding.patternSetupBack.setOnClickListener { finish() }

        viewBinding.patternLockViewSetup.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {}

            override fun onProgress(progressPattern: List<PatternLockView.Dot?>?) {}

            override fun onComplete(pattern: List<PatternLockView.Dot>) {
                if (pattern.size < 6) {
                    toast(R.string.login_pattern_lt_six_dots)
                } else if (pattern.size > 16) {
                    toast(R.string.login_pattern_bt_dots)
                } else {
                    val dotSet = HashSet<Int>()
                    pattern.forEach { dot ->
                        dotSet.add(dot.id)
                    }

                    if (dotSet.size < 6) {
                        toast(R.string.login_pattern_lt_six_dots)
                        return
                    }

                    processSetUp(pattern)
                }
            }


            override fun onCleared() {}
        })
        textSetUp()
    }

    private fun textSetUp() {
        if (isConfirm) {
            viewBinding.patternSetupTitle.text = getString(R.string.pattern_setup_confirm_title)
            viewBinding.patternSetupBody.text = getString(R.string.pattern_setup_confirm_body)
        } else {
            viewBinding.patternSetupTitle.text = getString(R.string.pattern_setup_title)
            viewBinding.patternSetupBody.text = getString(R.string.pattern_setup_body)
        }
    }

    private fun processSetUp(pattern: List<PatternLockView.Dot>) {
        val fid =
            SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, "")

        if (!isConfirm) {
            // 第一次
            originalPattern = Utils.patternToSha256(
                viewBinding.patternLockViewSetup,
                pattern.toMutableList(),
                fid
            )
            viewBinding.patternLockViewSetup.clearPattern()

            isConfirm = true
        } else {
            // 正在確認是否與第一次相同
            val newPattern = Utils.patternToSha256(
                viewBinding.patternLockViewSetup,
                pattern.toMutableList(),
                fid
            )

            if (TextUtils.equals(originalPattern, newPattern)) {
                callSetPatternApi(newPattern)
            } else {
                // 不相同
                showFailed(getString(R.string.pattern_setting_different))
            }

        }
        textSetUp()
    }

    private fun callSetPatternApi(newPattern: String) {
        val url = EnvironmentManager.getEnvironmentConfig().cssoUrl + "rest/setPatternLock"
        val userName = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_PID, "")

        //TODO 設定PatternLock 目前無API
        // 模擬API結果成功

        CommonDialog(this).apply {
            oneButtonInit(
                title = getString(R.string.change_success),
                body = getString(R.string.change_success_text),
                image = R.drawable.alert_2,
                showButtons = true,
                canceledOnTouchOutside = false,
                text = getString(R.string.close),
                onClick = {
                    val intent = Intent()
                    intent.putExtra(KEY_IS_GRAPHICAL_LOGIN_SET, true)
                    intent.putExtra(KEY_IS_GRAPHICAL_LOGIN_CHANGED, true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            )
        }.show()
    }

    private fun showFailed(body: String) {
        CommonDialog(this).apply {
            oneButtonInit(
                title = getString(R.string.setup_fail),
                body = body,
                image = R.drawable.alert_3,
                showButtons = true,
                canceledOnTouchOutside = false,
                text = getString(R.string.close),
                onClick = {
                    isConfirm = false
                    viewBinding.patternLockViewSetup.clearPattern()
                    textSetUp()
                }
            )
        }.show()
    }
}