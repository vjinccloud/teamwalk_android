package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.remote.response.fitbit.FitbitGetTokenResponse
import com.taiwanlife.teamwalk.remote.service.FitBitService
import retrofit2.Response

class FitbitRepository(
    private val fitBitService: FitBitService
) {

    fun getGrantType(): String {
        return "authorization_code"
    }

    fun getRedirectUrl(): String {
        return "teamwalk${BuildConfig.BUILD_TYPE}://webconnect?device=fitbit"
    }

    suspend fun getToken(
        authorization: String,
        code: String,
        grantType: String,
        clientId: String,
        redirectURI: String
    ): Response<FitbitGetTokenResponse> {
        return fitBitService.getToken(authorization, code, grantType, clientId, redirectURI)
    }
}