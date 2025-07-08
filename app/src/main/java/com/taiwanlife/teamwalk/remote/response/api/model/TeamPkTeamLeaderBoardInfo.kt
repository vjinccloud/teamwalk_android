package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TeamPkTeamLeaderBoardInfo(
    /**
     * 團體團隊ID
     */
    @SerializedName("team_id")
    val teamId: String,
    /**
     * 團隊頭像
     */
    @SerializedName("avatar")
    val avatar: String,
    /**
     * 團隊名稱
     */
    @SerializedName("name")
    val name: String,
    /**
     * 團隊目前總步數
     */
    @SerializedName("step")
    val step: Int,
    /**
     * 團隊人數上限
     */
    @SerializedName("max_people_num")
    val maxPeopleNum: Int,
    /**
     * 目前團隊人數
     */
    @SerializedName("current_people_num")
    val currentPeopleNum: Int
)