package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName


data class LandingResponse(
    /**
     * 暱稱
     */
    @SerializedName("nick_name")
    val nick_name: String?,
    @SerializedName("nickName")
    val nickName: String?,
    @SerializedName("nickname")
    val nickname: String?,
    /**
     * 推薦碼
     */
    @SerializedName("referrer_code")
    val referrerCode: String?,
    /**
     * 是否已完成導覽流程
     */
    @SerializedName("complete_onboarding")
    val completeOnboarding: Boolean?,

    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    @SerializedName("jti")
    val jti: String?,
) {
    fun getAvailableNickName():String {
        return nick_name ?: nickName ?: nickname ?: ""
    }
}
