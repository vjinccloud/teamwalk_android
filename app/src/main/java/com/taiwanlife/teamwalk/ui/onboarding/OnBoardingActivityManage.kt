package com.taiwanlife.teamwalk.ui.onboarding


object OnBoardingActivityManage {
    private val activities = mutableListOf<OnBoardingActivity<*>>()
    fun add(activity: OnBoardingActivity<*>) = activities.add(activity)
    fun remove(activity: OnBoardingActivity<*>) = activities.remove(activity)
    fun finishAll() {
        activities.forEach { it.finish() }
        activities.clear()
    }
}