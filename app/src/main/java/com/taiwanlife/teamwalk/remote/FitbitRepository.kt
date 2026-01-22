package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.MyApplication
import com.taiwanlife.teamwalk.remote.response.fitbit.FitbitGetTokenResponse
import com.taiwanlife.teamwalk.remote.service.FitBitService
import retrofit2.Response

class FitbitRepository(
    private val fitBitService: FitBitService
) {

    fun getGrantType(): String {
        return "authorization_code"
    }

    fun getRedirectUrl(redirectScheme: String): String {
        return "${redirectScheme}://webconnect?device=fitbit"
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