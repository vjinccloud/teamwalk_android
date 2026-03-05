package com.taiwanlife.teamwalk.ui.login

import kotlinx.serialization.SerialName

data class ChangeParams(
    @SerialName("service_id")
    val serviceId: String,
    @SerialName("ticket")
    val ticket: String
)
