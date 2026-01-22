package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class FitbitData(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
)
