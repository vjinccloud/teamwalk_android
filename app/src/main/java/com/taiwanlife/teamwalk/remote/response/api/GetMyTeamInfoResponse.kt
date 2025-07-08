package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.TeamInviteMemberInfo
import com.taiwanlife.teamwalk.remote.response.api.model.TeamMemberInfo

data class GetMyTeamInfoResponse(
    /**
     * 隊伍頭像
     */
    @SerializedName("avatar")
    val avatar: String,
    /**
     * 隊伍名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 活躍度
     */
    @SerializedName("active_rate")
    val activeRate: Int,
    /**
     * 總步數
     */
    @SerializedName("step")
    val step: Int,
    /**
     * */
    @SerializedName("members")
    val members: List<TeamMemberInfo>,
    /**
     * */
    @SerializedName("invite")
    val invite: List<TeamInviteMemberInfo>
)