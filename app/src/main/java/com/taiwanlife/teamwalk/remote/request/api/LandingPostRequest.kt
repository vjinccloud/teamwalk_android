package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class LandingPostRequest(
    /**
     * 他人的推薦碼
     */
    @SerializedName("referrer_code")
    val referrerCode: String?,
    /**
     * 暱稱
     */
    @SerializedName("nickname")
    val nickname: String?,
    /**
     * 綁定Apple
     */
    @SerializedName("binding_apple")
    val bindingApple: Boolean?,
    /**
     * 綁定Google
     */
    @SerializedName("binding_android")
    val bindingAndroid: Boolean?,
    /**
     * 綁定Fibit
     */
    @SerializedName("binding_fibit")
    val bindingFibit: Boolean?,
    /**
     * 綁定Fibit Token
     */
    @SerializedName("binding_fibit_token")
    val bindingFibitToken: String?,
    /**
     * 綁定Garmin Token
     */
    @SerializedName("binding_garmin_token")
    val bindingGarminToken: String?
) : BaseRequest()