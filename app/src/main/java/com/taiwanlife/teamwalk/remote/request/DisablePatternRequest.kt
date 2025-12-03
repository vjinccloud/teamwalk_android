package com.taiwanlife.teamwalk.remote.request

import retrofit2.http.Field

data class DisablePatternRequest(
    @Field("personalId")
    val personalId: String
)
