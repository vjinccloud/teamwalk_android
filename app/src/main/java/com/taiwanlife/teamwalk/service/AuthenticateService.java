/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.AuthToken;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Vincent.Chen
 * @version 1
 * 
 * @date 2020/10/28
 */
public interface AuthenticateService {

    /**
     * 
     * @param authInfo
     * @return
     */
    @POST("auth/token")
    Call<AuthToken> authToken(@Header("Origin") String Origin, @Body RequestBody authInfo);
//    Call<AuthToken> authToken(@Body RequestBody authInfo);
}
