package com.taiwanlife.teamwalk.ui.login

import android.os.Bundle
import android.view.View
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityLoginSuccessBinding
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.getGson
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class LoginSuccessActivity: BaseActivity<ActivityLoginSuccessBinding>({ ActivityLoginSuccessBinding.inflate(it) }) {
    private var logRequest: LogRequest? = null
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        observeOnLifeCycle(loginViewModel.loginFlow, onError = {
            logRequest = null
            toLogin()
        }) { loginResponse ->
            SecuredPreferenceStoreManager.editAndApply {
                it.putBoolean(Config.SP_LOGIN_AUTH, true)
//                it.putBoolean(Config.PREF_LOGIN_AUTH, true)
//                it.putString(Config.PREF_LOGIN_TICKET, ticket!!)
//                it.putString(Config.PREF_LOGIN_USERNAME, pid)

                logRequest?.let { logRequest ->
                    if (logRequest.isRememberMe) {
//                    it.putString(Config.PREF_LOGIN_PID, pid)
                        it.putString(Config.SP_LOGIN_REMEMBER_PID, logRequest.applId)
                    } else {
//                    it.putString(Config.PREF_LOGIN_PID, "")
                        it.putString(Config.SP_LOGIN_REMEMBER_PID, "")
                    }
                    it.putString(Config.SP_PID, logRequest.applId)

                }

                logRequest = null
                it.putString(Config.SP_LOG_REQUEST, "")
                loginResponse.let { loginResponse ->
                    it.putString(Config.SP_LOGIN_JWT, loginResponse.token)
                }
            }

            // 登入完畢 取得使用者資訊
            postEvent(Config.EVENT_LOGIN_SUCCESS_START_LANDING, "")

            finish()
        }

        val logRequestJson = SecuredPreferenceStoreManager.getString(Config.SP_LOG_REQUEST, "")
        if (logRequestJson.isNotEmpty()) {
            logRequest = getGson().fromJson(logRequestJson, LogRequest::class.java)
            loginViewModel.login(logRequest!!.service, logRequest!!.applId, logRequest!!.ticket, logRequest!!.deviceId)
        } else {
            toLogin()
        }
    }

    private fun toLogin() {
        // 清除所有資料並開啟登入頁
        Utils.clearLoginData(this, null)
        postEvent(Config.EVENT_TO_LOGIN, "")

        finish()
    }
}

