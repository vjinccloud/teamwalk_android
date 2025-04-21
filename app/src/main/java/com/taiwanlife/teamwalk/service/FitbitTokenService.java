/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.FitbitToken;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/22
 */
public interface FitbitTokenService {

    /**
     *
     * @param authorization
     * @param code
     * @param grantType
     * @param clientId
     * @return
     */
    @FormUrlEncoded
    @POST("token")
    Call<FitbitToken> getTocken(@Header("Authorization") String authorization,
                                @Field("code") String code,
                                @Field("grant_type") String grantType,
                                @Field("client_id") String clientId,
                                @Field("redirect_uri") String redirectURI);
}
