/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/26
 */
public interface GarminTokenService {

    /**
     *
     * @param authorization
     * @return
     */
    @POST("request_token")
    Call<ResponseBody> getAuthCode(@Header("Authorization") String authorization);

    /**
     *
     * @param authorization
     * @return
     */
    @POST("access_token")
    Call<ResponseBody> getToken(@Header("Authorization") String authorization);
}
