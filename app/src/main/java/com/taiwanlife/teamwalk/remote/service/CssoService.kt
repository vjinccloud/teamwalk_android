package com.taiwanlife.teamwalk.remote.service

import com.taiwanlife.teamwalk.remote.request.DisablePatternRequest
import com.taiwanlife.teamwalk.remote.request.PatternRequest
import com.taiwanlife.teamwalk.remote.response.CSSOResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface CssoService {

    /**
     * 模擬CSSO
     */
    @FormUrlEncoded
    @POST("mock/csso")
    suspend fun cssoLogin(
        @Field("SYS_ID") SYS_ID: String,
        @Field("appl_id") appl_id: String,
        @Field("appl_pwd") appl_pwd: String,
        @Field("service") service: String,
    ): Response<String>


    /**
     * Pattern Login
     */
    @FormUrlEncoded
    @POST("patternLogin")
    suspend fun patternLogin(
        @Field("SYS_ID")
        sysId: String,
        @Field("userId")
        userId: String,
        @Field("pattern_path")
        patternPath: String,
        @Field("service")
        service: String
    ): Response<String>


    @POST("rest/setPatternLock")
    suspend fun setPatternLock(@Header("Cookie") cookie: String, @Body pattern: PatternRequest): Response<CSSOResponse>

    @POST("user/disable_graphical_login")
    suspend fun disablePatternLock(@Body disablePatternRequest: DisablePatternRequest): Response<CSSOResponse>
}