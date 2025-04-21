/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.content.Intent;

import androidx.fragment.app.DialogFragment;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/29
 */
public interface Login {

    /**
     *
     * @param title
     * @param msg
     * @param image
     * @param buttonText
     * @param positiveButtonText
     * @param positiveIntent
     * @param forResult
     * @param resultCode
     * @return
     */
    DialogFragment buildAlert(String title, String msg, int image, String buttonText, String positiveButtonText, Intent positiveIntent, boolean forResult, int resultCode);

    /**
     *
     * @param dialogFragment
     */
    void showAlert(DialogFragment dialogFragment);
}
