package com.taiwanlife.teamwalk.remote.service

import com.taiwanlife.teamwalk.remote.request.LandingRequest
import com.taiwanlife.teamwalk.remote.request.LoginRequest
import com.taiwanlife.teamwalk.remote.response.api.PersonalPKResponse
import com.taiwanlife.teamwalk.remote.response.api.PersonalTeamResponse
import com.taiwanlife.teamwalk.remote.response.api.IndexResponse
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.LoginResponse
import com.taiwanlife.teamwalk.remote.response.api.ResponseWrapper
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {

    /**
     * 使用者登入
     */
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseWrapper<LoginResponse>>


    /**
     * Landing頁面 儲存個人資料
     */
    @POST("landing")
    suspend fun landing(@Body landingRequest: LandingRequest): Response<ResponseWrapper<Unit>>

    /**
     * 取得使用者資訊
     */
    @GET("user-info")
    suspend fun userInfo() : Response<ResponseWrapper<UserInfoResponse>>

    /**
     * 取得首頁資料
     */
    @GET("index")
    suspend fun index() : Response<ResponseWrapper<IndexResponse>>

    /**
     * 取得個人對戰資料
     */
    @GET("personal-pk")
    suspend fun personalPK() : Response<ResponseWrapper<PersonalPKResponse>>

    /**
     * 取得團隊對戰資料
     */
    @GET("personal-team")
    suspend fun personalTeam() : Response<ResponseWrapper<PersonalTeamResponse>>
}