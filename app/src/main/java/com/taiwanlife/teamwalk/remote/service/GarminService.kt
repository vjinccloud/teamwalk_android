package com.taiwanlife.teamwalk.remote.service

import com.taiwanlife.teamwalk.remote.request.garmin.GarminAccessTokenRequest
import com.taiwanlife.teamwalk.remote.response.garmin.GarminAccessTokenResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface GarminService {
    /**
     *
     * @param authorization
     * @return
     */
    @POST("request_token")
    suspend fun getAuthCode(@Header("Authorization") authorization: String): Response<ResponseBody>

    /**
     *
     * @param authorization
     * @return
     */
    @POST("access_token")
    suspend fun getToken(@Header("Authorization") authorization: String): Response<ResponseBody>

    @FormUrlEncoded
    @POST("https://diauth.garmin.com/di-oauth2-service/oauth/token")
    suspend fun getToken2(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code",
    ): Response<GarminAccessTokenResponse>
}