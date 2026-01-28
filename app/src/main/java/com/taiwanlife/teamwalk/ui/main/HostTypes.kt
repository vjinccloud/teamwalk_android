package com.taiwanlife.teamwalk.ui.main

enum class HostTypes(val value: String) {
    HOME("home"),
    LOGIN("login"),
    LOGIN_SUCCESS("loginsuccess"),
    LOGIN_FAILURE("loginfailure");


    companion object {
        fun getFromValue(value: String): HostTypes? {
            return HostTypes.entries.find { it.value == value }
        }
    }
}