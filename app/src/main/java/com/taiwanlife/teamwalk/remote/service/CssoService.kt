package com.taiwanlife.teamwalk.remote.service

import com.taiwanlife.teamwalk.remote.response.api.ResponseWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CssoService {
    /**
     * 模擬CSSO
     */
    @POST("mock/csso")
    suspend fun cssoLogin(
        @Query("SYS_ID") SYS_ID: String,
        @Query("appl_id") appl_id: String,
        @Query("appl_pwd") appl_pwd: String,
        @Query("service") service: String,
    ): Response<String>

    @GET("patternLogin")
    suspend fun cssoPatternLogin(
        @Query("SYS_ID") SYS_ID: String,
        @Query("userId") userId: String,
        @Query("pattern_path") pattern_path: String,
        @Query("service") service: String,
    ): Response<String>
}