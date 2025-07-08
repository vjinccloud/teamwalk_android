package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class CancelInviteTeamPkPostRequest(
    /**
     * 團隊組隊邀請代碼
     */
    @SerializedName("team_invite_id")
    val teamInviteId: String
) : BaseRequest()