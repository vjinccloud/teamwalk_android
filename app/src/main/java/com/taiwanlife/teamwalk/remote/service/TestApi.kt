package com.taiwanlife.teamwalk.remote.service

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface TestApi {
    @GET("get")
    suspend fun getSomething(): Response<ResponseBody>
}