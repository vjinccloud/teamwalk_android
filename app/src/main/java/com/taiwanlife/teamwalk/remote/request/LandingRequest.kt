package com.taiwanlife.teamwalk.remote.request

data class LandingRequest(
    val referrer_code: String,
    val nickname: String,
    val binding_apple: Boolean,
    val binding_android: Boolean,
    val binding_fibit: Boolean,
    val binding_fibitToken: String,
    val binding_garminToken: String
)
