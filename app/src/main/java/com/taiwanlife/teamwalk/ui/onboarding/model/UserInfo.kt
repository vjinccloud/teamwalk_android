package com.taiwanlife.teamwalk.ui.onboarding.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("referrerCode")
    val referrerCode: String = "",

    @SerializedName("nickname")
    val nickname: String = "",

    @SerializedName("bindingType")
    val bindingType: String? = null,

    @SerializedName("bindingToken")
    val bindingToken: String? = null,

    // 新的API沒這個欄位 不知道是否後面會需要
    @SerializedName("userAvatar")
    val userAvatar: String? = null,


    @SerializedName("accessToken")
    val accessToken: String = "",

    @SerializedName("refreshToken")
    val refreshToken: String = "",

    @SerializedName("jti")
    val jti: String = "",
)