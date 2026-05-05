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
import timber.log.Timber

class LoginViewModel(repository: Repository) : BaseViewModel(repository) {

    companion object {
        // getSysParam throttle：避免任何 bug 觸發 loop 時 DDoS 自家 server
        @Volatile
        private var lastSysParamTime = 0L
        private const val SYS_PARAM_THROTTLE_MS = 5_000L
    }

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
        // throttle 防呆：5 秒內重複呼叫直接擋掉，避免任何 bug 觸發循環時 DDoS server
        val now = System.currentTimeMillis()
        val sinceLast = now - lastSysParamTime
        if (sinceLast < SYS_PARAM_THROTTLE_MS) {
            Timber.w("getSysParam throttled: only ${sinceLast}ms since last call")
            return
        }
        lastSysParamTime = now
        systemParamFlow.execute {
            repository.api.getSysParam()
        }
    }

}