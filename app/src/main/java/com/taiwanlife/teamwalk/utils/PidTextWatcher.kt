package com.taiwanlife.teamwalk.utils

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import java.util.Arrays
import java.util.concurrent.ThreadLocalRandom.current

@SuppressLint("SetTextI18n")
class PidTextWatcher(private val editText: EditText, private val pid: CharArray, private val valid: (String) -> Unit) : TextWatcher {
    val saved = CharArray(10)
    init {
        if(pid.count { it != '\u0000' } == 10) {
            val p = String(pid)
            editText.setText("${p.substring(0, 3)}*****${p.substring(8)}")
            p.toCharArray(saved, 0)
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
        if(editText.text.trim().length != 10) return
        val current = editText.text.trim()
        if(current.length == 10) {
            // 檢查是不是亂打 只接受index3-7為 *
            var modified = false
            current.forEachIndexed { index, char ->
                when(index) {
                    in 0..2, in 8..9 -> {
                        if(current[index] != saved[index]) {
                            modified = true
                            return@forEachIndexed
                        }
                    }
                    in 3..7 -> {
                        if(current[index] != saved[index] && current[index] != '*') {
                            modified = true
                            return@forEachIndexed
                        }
                    }
                }
            }
            if(modified) {
                // 修改過了 且跟原本的不相同 他打什麼就給什麼
                valid(current.toString())
            } else {
                // 沒修改過 或是把*改成正確的
                valid(String(saved))
            }
        } else {
            valid(current.toString())
        }
    }

    fun clear() {
        Arrays.fill(saved, '\u0000')
    }
}