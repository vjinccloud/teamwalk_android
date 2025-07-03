package com.taiwanlife.teamwalk.remote.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
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
}