package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.remote.service.GarminService
import okhttp3.ResponseBody
import retrofit2.Response

class GarminRepository(
    private val garminService: GarminService
) {
    suspend fun getAuthCode(authorization: String): Response<ResponseBody> {
        return garminService.getAuthCode(authorization)
    }

    suspend fun getToken(authorization: String): Response<ResponseBody> {
        return garminService.getToken(authorization)
    }
}