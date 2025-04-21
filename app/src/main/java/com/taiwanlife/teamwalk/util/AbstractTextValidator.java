/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public abstract class AbstractTextValidator implements TextWatcher {
    public final TextView textView;

    /**
     *
     * @param textView
     */
    public AbstractTextValidator(TextView textView) {
        this.textView = textView;
    }

    /**
     *
     * @param textView
     * @param text
     */
    public abstract void validate(TextView textView, String text);

    /**
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        String text = textView.getText().toString().trim();
        validate(textView, text);
    }

    /**
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // no use
    }

    /**
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // no use
    }
}
