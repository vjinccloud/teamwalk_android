package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.remote.response.garmin.GarminAccessTokenResponse
import com.taiwanlife.teamwalk.remote.service.GarminService
import okhttp3.ResponseBody
import retrofit2.Response

class GarminRepository(
    private val garminService: GarminService
) {
    suspend fun getToken2(
        redirectUri: String,
        code: String,
        verifier: String
    ): Response<GarminAccessTokenResponse> {
        return garminService.getToken2(
            EnvironmentManager.getEnvironmentConfig().connectGarminConsumerKey,
            EnvironmentManager.getEnvironmentConfig().connectGarminConsumerSecret,
            code,
            verifier,
            redirectUri,
        )
    }
}