package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName

data class PersonalPkInfoResponse(
    /**
     * 個人PK賽ID
     */
    @SerializedName("pk_id")
    val pkId: String,
    /**
     * 對戰天數
     */
    @SerializedName("period")
    val period: Int,
    /**
     * 對戰步數
     */
    @SerializedName("steps")
    val steps: Int,
    /**
     * 押注龍珠數
     */
    @SerializedName("coins")
    val coins: Int,
    /**
     * 剩餘天數
     */
    @SerializedName("remaining_days")
    val remainingDays: Int,
    /**
     * 自己當前步數
     */
    @SerializedName("my_steps")
    val mySteps: Int,
    /**
     * 對手當前步數
     */
    @SerializedName("target_steps")
    val targetSteps: Int,
    /**
     * 對手暱稱
     */
    @SerializedName("target_nickname")
    val targetNickname: String,
    /**
     * 對手頭像路徑
     */
    @SerializedName("target_avatar")
    val targetAvatar: String,
    /**
     * 狀態(P:進行中,C:待確認,E:已結束,X:已過期,R:已拒絕,N:已取消)
     */
    @SerializedName("status")
    val status: String,
    /**
     * 邀請挑戰幾天後失效(當status為C才會有值)
     */
    @SerializedName("invite_expire_day")
    val inviteExpireDay: Int,
    /**
     * 邀請方向(當status為C才會有值，I：主動、P：被動)
     */
    @SerializedName("confirm_flag")
    val confirmFlag: String,
    /**
     * PK結束時間(當status為E才會有值)
     */
    @SerializedName("pk_end_time")
    val pkEndTime: String
)