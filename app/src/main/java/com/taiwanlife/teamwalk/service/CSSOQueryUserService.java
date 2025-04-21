/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.CSSOQueryUserBody;
import com.taiwanlife.teamwalk.model.CSSOUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public interface CSSOQueryUserService {

    public static final String QUERY_USER_RSP_CODE_SUCCESS = "0000";
    public static final String QUERY_USER_RSP_CODE_UNAUTHORIZED = "'0401";
    public static final String QUERY_USER_RSP_CODE_USER_NOT_FOUND = "0404";

    /**
     *
     * @param queryUser
     * @return
     */
    @POST("rest/queryUser")
    Call<CSSOUser> queryUser(@Body CSSOQueryUserBody queryUser);
}
