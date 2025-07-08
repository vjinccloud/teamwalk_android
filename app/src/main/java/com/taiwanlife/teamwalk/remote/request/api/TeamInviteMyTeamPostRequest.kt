package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class TeamInviteMyTeamPostRequest(
    /**
     * 團隊組隊邀請代碼
     */
    @SerializedName("team_invite_id")
    val teamInviteId: String,
    /**
     * 接受或拒絕(Y：接受 / N拒絕)
     */
    @SerializedName("result")
    val result: String
) : BaseRequest()