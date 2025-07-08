package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class JoinTeamPkInfo(
    /**
     * 團體團隊邀請ID
     */
    @SerializedName("team_invite_id")
    val teamInviteId: String,
    /**
     * 活動名稱
     */
    @SerializedName("team_act_name")
    val teamActName: String,
    /**
     * 受邀會員名稱
     */
    @SerializedName("target_friend_name")
    val targetFriendName: String,
    /**
     * 受邀會員頭像
     */
    @SerializedName("target_friend_avator")
    val targetFriendAvator: String,
    /**
     * 隊伍人數
     */
    @SerializedName("accu_member_cnt")
    val accuMemberCnt: Int,
    /**
     * 隊伍人數上限
     */
    @SerializedName("max_people_num")
    val maxPeopleNum: Int,
    /**
     * 隊伍人數下限
     */
    @SerializedName("min_people_num")
    val minPeopleNum: Int,
    /**
     * */
    @SerializedName("recent_join")
    val recentJoin: List<String>
)