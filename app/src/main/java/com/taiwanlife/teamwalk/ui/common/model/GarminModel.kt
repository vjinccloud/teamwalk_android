package com.taiwanlife.teamwalk.ui.common.model

import com.google.gson.annotations.SerializedName

/**
 * 拿來送到JS介面的物件
 */
data class GarminModel(
    @SerializedName("oauthToken")
    val oauthToken: String,
    @SerializedName("oauthTokenSecret")
    val oauthTokenSecret: String,
)