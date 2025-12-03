package com.taiwanlife.teamwalk.test

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityTestBinding
import com.taiwanlife.teamwalk.ui.login.LoginViewModel
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.forEach
import kotlin.getValue

class TestActivity: BaseActivity<ActivityTestBinding>({ ActivityTestBinding.inflate(it) }) {

    private val testViewModel: TestViewModel by viewModel()

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {

        viewBinding.originalPattern.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {}

            override fun onProgress(progressPattern: List<PatternLockView.Dot?>?) {}

            override fun onComplete(pattern: List<PatternLockView.Dot>) {
                val fid = SecuredPreferenceStoreManager.getString(
                    Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, ""
                )

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

                    val patternPath = Utils.patternToSha256(
                        viewBinding.originalPattern, pattern.toMutableList(), fid
                    )
                    viewBinding.originalPatternText.text = patternPath
                }
            }

            override fun onCleared() {}
        })
        viewBinding.newPattern.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {}

            override fun onProgress(progressPattern: List<PatternLockView.Dot?>?) {}

            override fun onComplete(pattern: List<PatternLockView.Dot>) {
                val fid = SecuredPreferenceStoreManager.getString(
                    Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, ""
                )

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

                    val patternPath = Utils.patternToSha256(
                        viewBinding.newPattern, pattern.toMutableList(), fid
                    )
                    viewBinding.newPatternText.text = patternPath
                }
            }

            override fun onCleared() {}
        })

        viewBinding.send.setOnClickListener {
            val originalPath = viewBinding.originalPatternText.text.toString()
            val newPath = viewBinding.newPatternText.text.toString()

            testViewModel.testPattern(originalPath, newPath)
        }

        viewBinding.disable.setOnClickListener {
            testViewModel.disablePattern()
        }

        observeOnLifeCycle(testViewModel.patternFlow) {
            Toast.makeText(this, "${it.rspCode} - ${it.rspMsg}", Toast.LENGTH_SHORT).show()
        }
    }
}