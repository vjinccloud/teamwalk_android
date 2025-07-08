package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TeamPkMapTeamInfo(
    /**
     * 團隊頭像
     */
    @SerializedName("avatar")
    val avatar: String,
    /**
     * 團隊名稱
     */
    @SerializedName("name")
    val name: String
)