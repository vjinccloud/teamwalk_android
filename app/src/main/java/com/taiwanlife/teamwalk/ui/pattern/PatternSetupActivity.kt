package com.taiwanlife.teamwalk.ui.pattern

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityPatternSetupBinding
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.ui.pattern.PatternSetupActivity.PatternProgress.*
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class PatternSetupActivity :
    BaseActivity<ActivityPatternSetupBinding>({ ActivityPatternSetupBinding.inflate(it) }) {

    companion object {
        const val KEY_ATTEMPT_ENABLE = "KEY_ATTEMPT_ENABLE"
        const val KEY_IS_GRAPHICAL_LOGIN_SET = "KEY_IS_GRAPHICAL_LOGIN_SET"
    }

    enum class PatternProgress {
        ORIGINAL,
        NEW_FIRST,
        NEW_SECOND
    }

    private val patternSetupViewModel: PatternSetupViewModel by viewModel()
    private var attemptEnable: Boolean = true
    private var patternProgress: PatternProgress = PatternProgress.ORIGINAL

    // 使用者是否再確認Pattern與第一次相符
    private var originalPattern: String = ""
    private var firstNewPattern: String = ""
    private var secondNewPattern: String = ""

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        attemptEnable = intent.getBooleanExtra(KEY_ATTEMPT_ENABLE, true)

        viewBinding.patternSetupBack.setOnClickListener {
            val intent = Intent()
            intent.putExtra(KEY_IS_GRAPHICAL_LOGIN_SET, false)
            setResult(RESULT_OK, intent)
            finish()
        }
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
        observeOnLifeCycle(patternSetupViewModel.patternFlow) { cssoResponse ->
            if(cssoResponse.rspCode == "0000") {
                showSuccess()
            } else {
                showFailed(getString(R.string.setup_fail_msg)) {
                    val intent = Intent()
                    intent.putExtra(KEY_IS_GRAPHICAL_LOGIN_SET, false)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun textSetUp() {
        when (patternProgress) {
            ORIGINAL -> {
                viewBinding.patternSetupTitle.text = getString(R.string.pattern_setup_original_title)
                viewBinding.patternSetupBody.text = getString(R.string.pattern_setup_original_body)
            }
            NEW_FIRST -> {
                viewBinding.patternSetupTitle.text = getString(R.string.pattern_setup_title)
                viewBinding.patternSetupBody.text = getString(R.string.pattern_setup_body)
            }
            NEW_SECOND -> {
                viewBinding.patternSetupTitle.text = getString(R.string.pattern_setup_confirm_title)
                viewBinding.patternSetupBody.text = getString(R.string.pattern_setup_confirm_body)
            }
        }
    }

    private fun processSetUp(pattern: List<PatternLockView.Dot>) {
        val fid =
            SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, "")
        val patternPath = Utils.patternToSha256(
            viewBinding.patternLockViewSetup,
            pattern.toMutableList(),
            fid
        )
        when (patternProgress) {
            ORIGINAL -> {
                originalPattern = patternPath
                patternProgress = NEW_FIRST
            }
            NEW_FIRST -> {
                firstNewPattern = patternPath
                patternProgress = NEW_SECOND
            }
            NEW_SECOND -> {
                if(TextUtils.equals(firstNewPattern, patternPath)){
                    secondNewPattern = patternPath
                    callSetPatternApi()
                } else {
                    // 不相同
                    showFailed(getString(R.string.pattern_setting_different)) {
                        patternProgress = NEW_FIRST
                        textSetUp()
                    }
                }
            }
        }
        viewBinding.patternLockViewSetup.clearPattern()
        textSetUp()
    }

    private fun callSetPatternApi() {
        val castGC = SecuredPreferenceStoreManager.getString(Config.SP_CASTGC, "")
        val userName = SecuredPreferenceStoreManager.getString(Config.SP_PID, "")

        when (BuildConfig.BUILD_TYPE) {
            "debug" -> {
                // 沒有CSSO 模擬成功
                showSuccess()
            }

            else -> {
                patternSetupViewModel.setPatternLock(castGC, userName, originalPattern, secondNewPattern)
            }
        }
    }

    private fun showFailed(body: String, callback: () -> Unit) {
        CommonDialog(this).apply {
            oneButtonInit(
                title = getString(R.string.setup_fail),
                body = body,
                image = R.drawable.alert_3,
                showButtons = true,
                canceledOnTouchOutside = false,
                text = getString(R.string.close),
                onClick = {
                    callback()
                }
            )
        }.show()
    }

    private fun showSuccess() {
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
                    setResult(RESULT_OK, intent)
                    finish()
                }
            )
        }.show()
    }
}