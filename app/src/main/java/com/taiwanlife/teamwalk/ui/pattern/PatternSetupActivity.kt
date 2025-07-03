package com.taiwanlife.teamwalk.ui.pattern

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
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

class PatternSetupActivity :
    BaseActivity<ActivityPatternSetupBinding>({ ActivityPatternSetupBinding.inflate(it) }) {

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
                    Toast.makeText(
                        this@PatternSetupActivity,
                        getString(R.string.login_pattern_lt_six_dots),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (pattern.size > 16) {
                    Toast.makeText(
                        this@PatternSetupActivity,
                        getString(R.string.login_pattern_bt_dots),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val dotSet = HashSet<Int>()
                    pattern.forEach { dot ->
                        dotSet.add(dot.id)
                    }

                    if (dotSet.size < 6) {
                        Toast.makeText(
                            this@PatternSetupActivity,
                            getString(R.string.login_pattern_lt_six_dots),
                            Toast.LENGTH_SHORT
                        ).show()
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
        val fid = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_FID, "")

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
        val userName = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_USERNAME, "")

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
                    setResult(RESULT_OK)
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