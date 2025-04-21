/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.UserInfo;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/3
 */
public interface UserService {

    /**
     *
     * @param authorization
     * @return
     */
    @GET("user/info")
    Call<UserInfo> getUserInfo(@Header("Authorization") String authorization);

    /**
     *
     * @param authorization
     * @param userInfo
     * @return
     */
    @POST("user/update")
    Call<UserInfo> saveUserInfo(@Header("Authorization") String authorization,
                                @Body UserInfo userInfo);
    /**
     *
     * @param authorization
     * @param data
     * @return
     */
    @POST("user/sync_googlefit")
    Call<Object> syncGooglefit(@Header("Authorization") String authorization,
                                @Body Map<String, Object> data);
}
