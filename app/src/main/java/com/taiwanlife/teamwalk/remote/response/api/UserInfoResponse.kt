package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName


data class UserInfoResponse(
    /**
     * 使用者唯一ID
     */
    @SerializedName("id")
    val id: String,
    /**
     * 會員等級
     */
    @SerializedName("level")
    val level: Int,
    /**
     * 龍珠數
     */
    @SerializedName("coins")
    val coins: Int,
    /**
     * 龍珠到期日
     */
    @SerializedName("coins_expired_date")
    val coinsExpiredDate: String?,
    /**
     * 經驗值
     */
    @SerializedName("exp")
    val exp: Int,
    /**
     * 已獲得徽章數
     */
    @SerializedName("badge_amt")
    val badgeAmt: Int?,
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
     * 性別 (M/F/O)
     */
    @SerializedName("gender")
    val gender: String?,
    /**
     * 會員自行上傳頭像(路徑)
     */
    @SerializedName("custom_image")
    val customImage: String?,
    /**
     * 會員內建頭像
     */
    @SerializedName("buildin")
    val buildin: String?,
    /**
     * 會員內建濾鏡
     */
    @SerializedName("filter")
    val filter: String?,
    /**
     * 身高 (公分)
     */
    @SerializedName("height")
    val height: Float?,
    /**
     * 體重 (公斤)
     */
    @SerializedName("weight")
    val weight: Float?,
    /**
     * 手機號碼
     */
    @SerializedName("mobile")
    val mobile: String?,
    /**
     * 電子郵件
     */
    @SerializedName("email")
    val email: String?,
    /**
     * 所在城市
     */
    @SerializedName("city")
    val city: String?,
    /**
     * 區／鄉／鎮
     */
    @SerializedName("district")
    val district: String?,
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
    /**
     * 裝置系統 (iOS/Android)
     */
    @SerializedName("device_OS")
    val deviceOS: String?,
    /**
     * 是否綁定Apple
     */
    @SerializedName("binding_apple")
    val bindingApple: Boolean?,
    /**
     * 是否綁定Android
     */
    @SerializedName("binding_android")
    val bindingAndroid: Boolean?,
    /**
     * 是否綁定Fibit
     */
    @SerializedName("binding_fibit")
    val bindingFibit: Boolean?,
    /**
     * 是否綁定Garmin
     */
    @SerializedName("binding_garmin")
    val bindingGarmin: Boolean?,
    /**
     * 是否綁定圖形登入
     */
    @SerializedName("binding_captcha")
    val bindingCaptcha: Boolean?,
    /**
     * 是否開啟推播
     */
    @SerializedName("enable_notification")
    val enableNotification: Boolean?,
    /**
     * 超越其他用戶的比率(首頁使用)
     */
    @SerializedName("lead_rate")
    val leadRate: Int?,
    /**
     * BMI值，由後端直接計算(首頁使用)
     */
    @SerializedName("bmi")
    val bmi: Float?,
    /**
     * 連續簽到天數(我的頁面使用)
     */
    @SerializedName("continue_sign_up_count")
    val continueSignUpCount: Int,
    /**
     * 可兌換券(我的頁面使用)
     */
    @SerializedName("product_exchange_count")
    val productExchangeCount: Int?,
    /**
     * 每日目標步數
     */
    @SerializedName("goal_steps")
    val goalSteps: Int?,
    /**
     * 每日目標睡眠小時
     */
    @SerializedName("goal_sleep_hours")
    val goalSleepHours: Int?
) {
    fun getAvailableNickName():String {
        return nick_name ?: nickName ?: nickname ?: ""
    }
}
