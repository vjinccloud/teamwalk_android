package com.taiwanlife.teamwalk.ui.onboarding

import android.app.Activity


object OnBoardingActivityManage {
    private val activities = mutableListOf<Activity>()
    fun add(activity: Activity) = activities.add(activity)
    fun remove(activity: Activity) = activities.remove(activity)
    fun finishAll() {
        activities.forEach { it.finish() }
        activities.clear()
    }
}