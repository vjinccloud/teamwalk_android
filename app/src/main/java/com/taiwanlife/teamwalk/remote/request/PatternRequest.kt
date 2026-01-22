package com.taiwanlife.teamwalk.remote.request

import com.google.gson.annotations.SerializedName

data class PatternRequest (
    @SerializedName("userId")
    var userId: String? = null,
    @SerializedName("newPatternLock")
    var newPatternLock: String? = null,
    @SerializedName("origPatternLock")
    var origPatternLock: String? = null,
)