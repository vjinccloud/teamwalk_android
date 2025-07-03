package com.taiwanlife.teamwalk.utils

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val pkgName: String,
    val launchClassName: String,
    val appIcon: Drawable,
)