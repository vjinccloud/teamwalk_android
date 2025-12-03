package com.taiwanlife.teamwalk.remote.request

data class PatternRequest (
    var userId: String? = null,
    var newPatternLock: String? = null,
    var origPatternLock: String? = null,
)