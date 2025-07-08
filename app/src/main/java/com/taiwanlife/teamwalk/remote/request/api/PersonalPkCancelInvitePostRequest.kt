package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class PersonalPkCancelInvitePostRequest(
    /**
     * PK邀請代碼
     */
    @SerializedName("pk_id")
    val pkId: String
) : BaseRequest()