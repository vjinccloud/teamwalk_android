package com.taiwanlife.teamwalk.remote.response.api

import com.taiwanlife.teamwalk.remote.response.api.model.Competition
import kotlinx.serialization.SerialName

data class PersonalTeamResponse(
    @SerialName("competitions")
    val competitions: List<Competition>
)
