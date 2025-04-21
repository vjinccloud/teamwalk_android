/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.content.Intent;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public interface LoginMethod {
    /**
     *
     * @param loginContext
     */
    public void setOnFragmentAttachedListener(Login loginContext);

    /**
     *
     * @param pid
     * @param intent
     */
    public void queryUser(String pid, Intent intent);
}
