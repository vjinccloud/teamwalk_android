package com.taiwanlife.teamwalk.remote.request

class CssoLoginRequest(
    val SYS_ID: String,
    val appl_id: String,
    val appl_pwd: String,
    val service: String,
) : BaseRequest()