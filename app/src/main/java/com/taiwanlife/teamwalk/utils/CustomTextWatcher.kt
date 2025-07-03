package com.taiwanlife.teamwalk.utils

import android.text.Editable
import android.text.TextWatcher

class CustomTextWatcher(val afterTextChanged: () -> Unit) : TextWatcher {
    override fun beforeTextChanged(
        p0: CharSequence?,
        p1: Int,
        p2: Int,
        p3: Int
    ) {
    }

    override fun onTextChanged(
        p0: CharSequence?,
        p1: Int,
        p2: Int,
        p3: Int
    ) {
    }

    override fun afterTextChanged(p0: Editable?) {
        afterTextChanged()

    }
}