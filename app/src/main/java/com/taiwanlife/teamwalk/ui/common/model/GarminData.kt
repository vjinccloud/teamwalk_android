package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

data class GarminData(
    @SerializedName("tsGarmin")
    val tsGarmin: String? = null,
    @SerializedName("oauthToken")
    val oauthToken: String? = null,
    @SerializedName("oauthTokenSecret")
    val oauthTokenSecret: String? = null,
)