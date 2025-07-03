package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.remote.request.LandingRequest
import com.taiwanlife.teamwalk.remote.request.LoginRequest
import com.taiwanlife.teamwalk.remote.response.api.PersonalPKResponse
import com.taiwanlife.teamwalk.remote.response.api.PersonalTeamResponse
import com.taiwanlife.teamwalk.remote.response.api.IndexResponse
import com.taiwanlife.teamwalk.remote.response.api.LoginResponse
import com.taiwanlife.teamwalk.remote.response.api.ResponseWrapper
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import com.taiwanlife.teamwalk.remote.service.APIService
import retrofit2.Response

class ApiRepository(
    private val apiService: APIService
) {

    /**
     * 使用者登入
     */
    suspend fun login(
        ticket: String,
        service: String,
        appUuid: String,
        deviceId: String,
        pushId: String
    ): Response<ResponseWrapper<LoginResponse>> {
        return apiService.login(LoginRequest(ticket, service, appUuid, deviceId, pushId))
    }

    /**
     * Landing頁面 儲存個人資料
     */
    suspend fun landing(
        referrerCode: String,
        nickName: String,
        bindingApple: Boolean,
        bindingAndroid: Boolean,
        bindingFibit: Boolean,
        bindingFibitToken: String,
        bindingGarminToken: String,
    ): Response<ResponseWrapper<Unit>> {
        return apiService.landing(
            LandingRequest(
                referrerCode,
                nickName,
                bindingApple,
                bindingAndroid,
                bindingFibit,
                bindingFibitToken,
                bindingGarminToken
            )
        )
    }

    /**
     * 取得使用者資訊
     */
    suspend fun userInfo(): Response<ResponseWrapper<UserInfoResponse>>{
        return apiService.userInfo()
    }

    /**
     * 取得首頁資料
     */
    suspend fun index(): Response<ResponseWrapper<IndexResponse>> {
        return apiService.index()
    }

    /**
     * 取得個人對戰資料
     */
    suspend fun personalPK(): Response<ResponseWrapper<PersonalPKResponse>>{
        return apiService.personalPK()
    }

    /**
     * 取得團隊對戰資料
     */
    suspend fun personalTeam(): Response<ResponseWrapper<PersonalTeamResponse>>{
        return apiService.personalTeam()
    }
}