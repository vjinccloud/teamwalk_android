package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.InviteTeamPkInfo
import com.taiwanlife.teamwalk.remote.response.api.model.JoinTeamPkInfo
import com.taiwanlife.teamwalk.remote.response.api.model.TeamPkInfo

data class ListTeamPkInfoResponse(
    /**
     * */
    @SerializedName("all")
    val all: List<TeamPkInfo>,
    /**
     * */
    @SerializedName("my")
    val my: List<TeamPkInfo>,
    /**
     * */
    @SerializedName("invite")
    val invite: List<InviteTeamPkInfo>,
    /**
     * */
    @SerializedName("join")
    val join: List<JoinTeamPkInfo>
)