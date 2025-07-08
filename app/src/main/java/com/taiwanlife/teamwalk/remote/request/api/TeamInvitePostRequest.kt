package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class TeamInvitePostRequest(
    /**
     * 團賽代碼
     */
    @SerializedName("team_act_id")
    val teamActId: String?,
    /**
     * 會員代碼
     */
    @SerializedName("member_id")
    val memberId: String?
) : BaseRequest()