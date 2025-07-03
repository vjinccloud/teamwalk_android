package com.taiwanlife.teamwalk.remote.request

data class LoginRequest(
    val ticket: String,
    val service: String,
    val app_uuid: String,
    val device_id: String,
    val push_id: String
) : BaseRequest()