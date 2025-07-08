package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TeamInviteMemberInfo(
    /**
     * 團隊邀請代碼
     */
    @SerializedName("team_invite_id")
    val teamInviteId: String,
    /**
     * 團隊頭像
     */
    @SerializedName("avatar")
    val avatar: String,
    /**
     * 會員名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 推薦碼
     */
    @SerializedName("my_referrer_code")
    val myReferrerCode: String,
    /**
     * 平均日步數
     */
    @SerializedName("avg_step")
    val avgStep: Int,
    /**
     * */
    @SerializedName("is_leader")
    val isLeader: Any
)