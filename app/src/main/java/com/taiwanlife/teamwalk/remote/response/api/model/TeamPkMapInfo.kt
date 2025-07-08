package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class TeamPkMapInfo(
    /**
     * 地圖顯示級距(共10個)
     */
    @SerializedName("map_point")
    val mapPoint: Int,
    /**
     * */
    @SerializedName("teams")
    val teams: List<TeamPkMapTeamInfo>
)