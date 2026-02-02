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
     * example: APPLE|GOOGLE|GARMIN|FITBIT
     * 綁定哪種裝置
     */
    @SerializedName("binding_type")
    val bindingType: String?,
    /**
     * 綁定Fibit或Garmin 才有Token值
     */
    @SerializedName("binding_token")
    val bindingToken: String?,

    @SerializedName("accessToken")
    val accessToken: String?,

    @SerializedName("refreshToken")
    val refreshToken: String?,

    @SerializedName("jti")
    val jti: String?,
) : BaseRequest()