package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName


data class PkInfo(
    @SerializedName("pk_id")
    val pkId: String,
    @SerializedName("period")
    val period: Int,
    @SerializedName("steps")
    val steps: Int,
    @SerializedName("coins")
    val coins: Int,
    @SerializedName("remaining_days")
    val remainingDays: Int,
    @SerializedName("my_steps")
    val mySteps: Int,
    @SerializedName("target_steps")
    val targetSteps: Int,
    @SerializedName("target_nickname")
    val targetNickname: String,
    @SerializedName("target_avatar")
    val targetAvatar: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("invite_expire_day")
    val inviteExpireDay: Int,
    @SerializedName("confirm_flag")
    val confirmFlag: String,
    @SerializedName("pk_end_time")
    val pkEndTime: String
)