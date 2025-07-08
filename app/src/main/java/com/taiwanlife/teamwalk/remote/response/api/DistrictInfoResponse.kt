package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class DistrictInfoResponse(
    /**
     * 縣市
     */
    @SerializedName("city")
    val city: String,
    /**
     * 行政區
     */
    @SerializedName("district")
    val district: String
)