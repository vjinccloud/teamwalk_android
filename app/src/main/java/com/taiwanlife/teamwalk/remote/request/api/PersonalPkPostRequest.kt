package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class PersonalPkPostRequest(
    /**
     * PK對象會員代碼
     */
    @SerializedName("invited_member_id")
    val invitedMemberId: String,
    /**
     * 發起PK步數
     */
    @SerializedName("steps")
    val steps: Int,
    /**
     * 挑戰週期
     */
    @SerializedName("period")
    val period: Int,
    /**
     * 挑戰龍珠數
     */
    @SerializedName("coins")
    val coins: Int
) : BaseRequest()