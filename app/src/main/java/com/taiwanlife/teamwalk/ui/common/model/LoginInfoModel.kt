package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class LoginInfoModel(
    @SerializedName("jwt")
    val jwt: String
)
