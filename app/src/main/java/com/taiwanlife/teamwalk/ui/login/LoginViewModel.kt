package com.taiwanlife.teamwalk.ui.login

import com.google.gson.Gson
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.request.api.LoginRequest
import com.taiwanlife.teamwalk.remote.response.api.LoginResponse
import com.taiwanlife.teamwalk.remote.response.api.SysParamInfoResponse
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager

class LoginViewModel(repository: Repository) : BaseViewModel(repository) {
    val loginFlow = ApiFlow<LoginResponse>(this)
    val systemParamFlow = ApiFlow<SysParamInfoResponse>(this)

    fun getLogRequest(redirectScheme: String, userId: String, ticket: String, deviceId: String, isRememberMe: Boolean) :String {
        return Gson().toJson(LogRequest(
            userId,
            ticket,
            redirectScheme,
            deviceId,
            isRememberMe
        ))
    }

    fun login(redirectScheme: String, userId: String, ticket: String, deviceId: String) {
        val appUuid =
            SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, "")
        val pushId = SecuredPreferenceStoreManager.getString(Config.SP_FCM_IDENTIFIER, "")

        SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_LOG_REQUEST, "")
        loginFlow.execute { repository.api.login(userId, ticket, redirectScheme, appUuid, deviceId, pushId) }
    }

    fun getSysParam() {
        systemParamFlow.execute {
            repository.api.getSysParam()
        }
    }

}