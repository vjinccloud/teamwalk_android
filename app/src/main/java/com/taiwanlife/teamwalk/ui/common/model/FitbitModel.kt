package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class FitbitModel(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
)