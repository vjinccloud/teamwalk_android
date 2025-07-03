package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName


data class UserInfoResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("level")
    val level: Int,
    @SerializedName("coins")
    val coins: Int,
    @SerializedName("exp")
    val exp: Int,
    @SerializedName("badge_amt")
    val badgeAmt: Int,
    @SerializedName("nick_name")
    val nick_name: String?,
    @SerializedName("nickName")
    val nickName: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("custom_image")
    val customImage: String,
    @SerializedName("filter")
    val filter: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("mobile")
    val mobile: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("district")
    val district: String,
    @SerializedName("referrer_code")
    val referrerCode: String,
    @SerializedName("complete_onboarding")
    val completeOnboarding: Boolean,
    @SerializedName("device_OS")
    val deviceOS: String,
    @SerializedName("binding_apple")
    val bindingApple: Boolean,
    @SerializedName("binding_android")
    val bindingAndroid: Boolean,
    @SerializedName("binding_fibit")
    val bindingFibit: Boolean,
    @SerializedName("binding_garmin")
    val bindingGarmin: Boolean,
    @SerializedName("binding_captcha")
    val bindingCaptcha: Boolean,
    @SerializedName("enable_notification")
    val enableNotification: Boolean
) {
    fun getAvailableNickName():String {
        return nick_name ?: nickName ?: nickname ?: ""
    }
}
