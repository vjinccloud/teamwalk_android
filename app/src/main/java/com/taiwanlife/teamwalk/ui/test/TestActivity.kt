package com.taiwanlife.teamwalk.ui.test

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.htmlEncode
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityTestBinding
import com.taiwanlife.teamwalk.utils.FileLoggingTree
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class TestActivity : BaseActivity<ActivityTestBinding>({ ActivityTestBinding.inflate(it) }) {

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

        viewBinding.log.text = getFormattedLogs()
    }

    fun getFormattedLogs(): CharSequence {
        val file = File(filesDir, FileLoggingTree.LOG_FILE_NAME)

        if (!file.exists() || file.length() == 0L) {
            return "目前尚無 Log 記錄（或已被清理）"
        }

        return try {
            // 讀取最後 200 行，避免文字量過大導致 TextView 渲染緩慢
            val logs = file.readLines().takeLast(200)
            val builder = StringBuilder()

            logs.forEach { line ->
                val escapedLine = line.htmlEncode() // 避免 Log 內容破壞 HTML 結構
                when {
                    line.contains("[ERROR]") -> builder.append("<font color='#FF4444'><b>$escapedLine</b></font><br>")
                    line.contains("[WARN]") -> builder.append("<font color='#FFBB33'>$escapedLine</font><br>")
                    line.contains("[DEBUG]") -> builder.append("<font color='#99CC00'>$escapedLine</font><br>")
                    else -> builder.append("$escapedLine<br>")
                }
            }
            android.text.Html.fromHtml(builder.toString(), android.text.Html.FROM_HTML_MODE_LEGACY)
        } catch (e: Exception) {
            "讀取 Log 發生錯誤: ${e.message}"
        }
    }
}