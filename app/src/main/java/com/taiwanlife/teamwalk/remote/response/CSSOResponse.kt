package com.taiwanlife.teamwalk.remote.response

import retrofit2.http.Field

data class CSSOResponse(
    @Field("rspCode")
    val rspCode: String,
    @Field("rspMsg")
    val rspMsg: String
)