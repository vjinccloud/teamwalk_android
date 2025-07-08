package com.taiwanlife.teamwalk.ui.login

import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.api.LoginResponse
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager

class LoginViewModel(repository: Repository) : BaseViewModel(repository) {
    val ticketFlow = CSSOFlow(this)
    val patternFlow = CSSOFlow(this)
    val loginFlow = ApiFlow<LoginResponse>(this)

    fun getTicket(pid: String, pwd: String) {
        ticketFlow.execute {
            repository.cssoRepository.cssoLogin(
                Config.API_SYS_ID,
                pid,
                pwd,
                "teamwalk${BuildConfig.BUILD_TYPE}://loginsuccess"
            )
        }
    }

    fun patternLogin(userId: String, patternPath: String) {
        patternFlow.execute {
            repository.cssoRepository.cssoPatternLogin(
                Config.API_SYS_ID,
                userId,
                patternPath,
                "teamwalk${BuildConfig.BUILD_TYPE}://loginsuccess"
            )
        }
    }


    fun login(ticket: String, deviceId: String) {
        val appUuid =
            SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, "")
        val pushId = SecuredPreferenceStoreManager.getString(Config.SP_FCM_TOKEN, "")

        loginFlow.execute { repository.api.login(ticket, "teamwalk", appUuid, deviceId, pushId) }
    }
}