package com.taiwanlife.teamwalk.remote.response.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.response.api.model.Personal
import com.taiwanlife.teamwalk.remote.response.api.model.Team

data class GetPkSummaryInfoResponse(
    /**
     * */
    @SerializedName("personal")
    val personal: Personal,
    /**
     * */
    @SerializedName("team")
    val team: Team
)