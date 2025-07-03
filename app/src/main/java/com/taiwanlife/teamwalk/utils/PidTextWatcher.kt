package com.taiwanlife.teamwalk.utils

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText

@SuppressLint("SetTextI18n")
class PidTextWatcher(private val editText: EditText, private val pid: String, private val valid: (String) -> Unit) : TextWatcher {
    init {
        if(!TextUtils.isEmpty(pid) && pid.length == 10) {
            editText.setText("${pid.substring(0, 3)}*****${pid.substring(8)}")
        }
    }

    override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
    }

    override fun afterTextChanged(s: Editable?) {
        val current = editText.text.toString().trim()
        if(current.length != 10) return
        if(!TextUtils.isEmpty(pid) && pid.length == 10) {
            // 檢查是不是亂打 只接受index3-7為 *
            var modified = false
            current.forEachIndexed { index, char ->
                when(index) {
                    in 0..2, in 8..9 -> {
                        if(current[index] != pid[index]) {
                            modified = true
                            return@forEachIndexed
                        }
                    }
                    in 3..7 -> {
                        if(current[index] != pid[index] && current[index] != '*') {
                            modified = true
                            return@forEachIndexed
                        }
                    }
                }
            }
            if(modified) {
                // 修改過了 且跟原本的不相同 他打什麼就給什麼
                valid(current)
            } else {
                // 沒修改過 或是把*改成正確的
                valid(pid)
            }
        } else {
            valid(current)
        }
    }
}