package com.taiwanlife.teamwalk.remote.response

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field

data class CSSOResponse(
    @SerializedName("rspCode")
    @Field("rspCode")
    val rspCode: String,
    @SerializedName("rspMsg")
    @Field("rspMsg")
    val rspMsg: String?
)