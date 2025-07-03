package com.taiwanlife.teamwalk.remote.service

import com.taiwanlife.teamwalk.remote.response.fitbit.FitbitGetTokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface FitBitService {

    @FormUrlEncoded
    @POST("token")
    suspend fun getToken(
        @Header("Authorization") authorization: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("redirect_uri") redirectURI: String
    ): Response<FitbitGetTokenResponse>
}