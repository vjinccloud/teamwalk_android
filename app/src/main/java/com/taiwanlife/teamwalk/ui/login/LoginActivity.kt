package com.taiwanlife.teamwalk.ui.login

import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityLoginBinding
import com.taiwanlife.teamwalk.java_utils.DeviceUtil
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.utils.CustomTextWatcher
import com.taiwanlife.teamwalk.utils.PidTextWatcher
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.security.SecureRandom

class LoginActivity : BaseActivity<ActivityLoginBinding>({ ActivityLoginBinding.inflate(it) }) {

    companion object {
        private const val QUERY_PARAM_TICKET = "ticket"
    }

    override val statusBarColor: Int = R.color.white

    private val loginViewModel: LoginViewModel by viewModel()

    private var position = 0
    private var isRememberMe: Boolean = false
    private var pid: String = ""
    private var ticket: String? = ""
    private var genText: String = ""

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        DeviceUtil.setFlagSecure(this)

        observeOnLifeCycle(loginViewModel.loginFlow.sharedFlow) { loginResponse ->
            SecuredPreferenceStoreManager.editAndApply {
                it.putBoolean(Config.SP_LOGIN_AUTH, true)
//                it.putBoolean(Config.PREF_LOGIN_AUTH, true)
//                it.putString(Config.PREF_LOGIN_TICKET, ticket!!)
//                it.putString(Config.PREF_LOGIN_USERNAME, pid)

                if (isRememberMe) {
//                    it.putString(Config.PREF_LOGIN_PID, pid)
                    it.putString(Config.SP_LOGIN_PID, pid)
                } else {
//                    it.putString(Config.PREF_LOGIN_PID, "")
                    it.putString(Config.SP_LOGIN_PID, "")
                }

                loginResponse.let { loginResponse ->
                    it.putString(Config.SP_LOGIN_JWT_TOKEN, loginResponse.token)
                }
            }

            setResult(RESULT_OK)
            finish()
        }

        isRememberMe =
            SecuredPreferenceStoreManager.getBoolean(Config.SP_LOGIN_REMEMBER_ME, false)
        pid = if (isRememberMe) SecuredPreferenceStoreManager.getString(
            Config.SP_LOGIN_PID,
            ""
        ) else ""

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            viewBinding.loginBuildAppv.text = packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        var uuid = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_UUID, "")
        var showSecurity = SecuredPreferenceStoreManager.getBoolean(Config.SP_SHOW_SECURITY_ALERT_FIRST_TIME, true)
        if (showSecurity) {
//            uuid = SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, "")
//            SecuredPreferenceStoreManager.simpleEditAndApply(Config.PREF_LOGIN_UUID, uuid)
            val commonDialog = CommonDialog(this)
            commonDialog.oneButtonInit(
                title = "",
                body = getString(R.string.security_msg),
                image = R.drawable.alert_1,
                showButtons = true,
                canceledOnTouchOutside = true,
                text = getString(R.string.ok),
                onClick = {
                    SecuredPreferenceStoreManager.editAndApply {
                        it.putBoolean(Config.SP_SHOW_SECURITY_ALERT_FIRST_TIME, false)
                    }
                }
            )
            commonDialog.show()
        }

        setPidUI()
        setPasswordUI()
        setCaptcha()
        setPatterLock()
        viewBinding.loginButton.setOnClickListener {
            login()
        }

        tabSettings()
        // API 流程
        checkVersion()

        // 記住我
        viewBinding.loginCheckBoxRememberMe.isChecked = isRememberMe
        viewBinding.loginCheckBoxRememberMe.setOnCheckedChangeListener { button, isChecked ->
            isRememberMe = isChecked

            SecuredPreferenceStoreManager.editAndApply {
                it.putBoolean(Config.SP_LOGIN_REMEMBER_ME, isRememberMe)
            }
        }
        // 註冊
        setSpannable(
            viewBinding.loginSignupTextView,
            getString(R.string.sign_up),
            R.color.colorLinkText
        ) {
            toRegister()
        }
        // 忘記密碼
        setSpannable(
            viewBinding.loginForgetPassTextView,
            getString(R.string.forget_pass),
            R.color.colorLinkSecondaryText
        ) {
            toForgetPassword()
        }
    }

    private fun setPidUI() {
        viewBinding.loginEditTextPasswordPid.addTextChangedListener(
            PidTextWatcher(
                viewBinding.loginEditTextPasswordPid,
                pid,
                ::validPid
            )
        )
    }

    private fun setPasswordUI() {
        viewBinding.loginEditTextPassword.addTextChangedListener(CustomTextWatcher {
            viewBinding.loginTextViewWarningPassword.visibility = View.GONE
            viewBinding.loginLayoutPasswordPassword.background = null
        })
    }

    private fun setCaptcha() {
        viewBinding.loginEditTextCaptcha.addTextChangedListener(CustomTextWatcher {
            if (viewBinding.loginTextViewWarningCaptcha.isVisible) {
                viewBinding.loginLayoutPasswordCaptcha.background = null
                viewBinding.loginTextViewWarningCaptcha.visibility = View.GONE
            }
        })
        viewBinding.loginEditTextCaptcha.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.loginButtonCaptcha.setOnClickListener {
            genText = genRandomNumbers()
            viewBinding.loginButtonCaptcha.text = genText
        }

        genText = genRandomNumbers()
        viewBinding.loginButtonCaptcha.text = genText
    }

    private fun setPatterLock() {

        viewBinding.loginPatternLockView.isInStealthMode = true
        viewBinding.loginPatternLockView.isInputEnabled = isRememberMe && pid.length == 10

        viewBinding.loginPatternToggleStealthModeButton.setOnClickListener {
            viewBinding.loginPatternLockView.isInStealthMode =
                !viewBinding.loginPatternLockView.isInStealthMode
            if (viewBinding.loginPatternLockView.isInStealthMode) {
                viewBinding.loginPatternToggleStealthModeButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.visibility,
                    0
                )
                viewBinding.loginPatternToggleStealthModeButton.text =
                    getString(R.string.login_pattern_normal_mode)
            } else {
                viewBinding.loginPatternToggleStealthModeButton.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.visibility_off,
                    0
                )
                viewBinding.loginPatternToggleStealthModeButton.text =
                    getString(R.string.login_pattern_stealth_mode)
            }
        }

        viewBinding.loginPatternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {}

            override fun onProgress(progressPattern: List<PatternLockView.Dot?>?) {}

            override fun onComplete(pattern: List<PatternLockView.Dot>) {
                val fid = SecuredPreferenceStoreManager.getString(Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, "")
                val currentPid = viewBinding.loginEditTextPasswordPid.text.toString().trim()
                if (TextUtils.isEmpty(currentPid) || currentPid.length != 10) {
                    viewBinding.loginLayoutPasswordPid.setBackgroundColor(getColor(R.color.colorError))
                    viewBinding.loginLayoutPasswordPid.visibility
                    return
                }

                if (pattern.size < 6) {
                    toast(R.string.login_pattern_lt_six_dots)
                } else if (pattern.size > 16) {
                    toast(R.string.login_pattern_bt_dots)
                } else {
                    val dotSet = HashSet<Int>()
                    pattern.forEach { dot ->
                        dotSet.add(dot.id)
                    }

                    if (dotSet.size < 6) {
                        toast(R.string.login_pattern_lt_six_dots)
                        return
                    }

                    val patternPath = Utils.patternToSha256(
                        viewBinding.loginPatternLockView,
                        pattern.toMutableList(),
                        fid
                    )

                    // 新API 拿Ticket 然後登入取JWT
                    loginViewModel.getTicket(currentPid, patternPath)
                    observeOnLifeCycle(
                        loginViewModel.ticketFlow.sharedFlow,
                        unSubscribeOnComplete = true
                    ) { ticketUrl ->
                        val uri = ticketUrl.toUri()
                        ticket = uri.getQueryParameter(QUERY_PARAM_TICKET)

                        // 以下目前還沒通 打Login
//                        if (!TextUtils.isEmpty(ticket)) {
//                            // 拿到ticket
//                            loginViewModel.patternLogin(currentPid, patternPath)
//                        }
                        loginViewModel.login(
                            ticket!!,
                            Utils.getDeviceId(this@LoginActivity)
                        )
                    }
                }
            }

            override fun onCleared() {}
        })
    }

    private fun checkVersion() {
        //TODO 確認版本號碼 目前無API

        getAnnouncement()
    }

    private fun getAnnouncement() {
        //TODO 取得公告 目前無API
    }

    private fun validPid(pidText: String) {
        // 到這邊表示pid OK
        pid = pidText

        viewBinding.loginTextViewWarningPid.visibility = View.GONE
        viewBinding.loginLayoutPasswordPid.background = null

        queryUser()
    }

    /**
     * 觀察舊程式碼之後 這支應該是拿來判定使用者的和是否有會籍和圖形碼相關的部分
     */
    private fun queryUser() {
        //TODO queryUser 目前無API
        //這裡將成功的結果搬過來
        val QUERY_USER_RSP_CODE_SUCCESS = "0000"
        val QUERY_USER_RSP_CODE_UNAUTHORIZED = "0401"
        val QUERY_USER_RSP_CODE_USER_NOT_FOUND = "0404"
        val rspCode: String = //cssoUser.getRspCode()
            when (pid.last()) {
                '1' -> {
                    QUERY_USER_RSP_CODE_UNAUTHORIZED
                }

                '4' -> {
                    QUERY_USER_RSP_CODE_USER_NOT_FOUND
                }

                else -> {
                    QUERY_USER_RSP_CODE_SUCCESS
                }
            }

        if (!TextUtils.isEmpty(rspCode) && rspCode == QUERY_USER_RSP_CODE_SUCCESS) {
            Timber.d("Query user found")


            val patternLockStatus = "Y" //cssoUser.getPatternLockStatus()
            if (TextUtils.equals(patternLockStatus, "Y") || TextUtils.equals(
                    patternLockStatus,
                    "O"
                )
            ) {
//                SecuredPreferenceStoreManager.editAndApply { editor ->
//                    editor.putBoolean(Config.PREF_LOGIN_PATTERN_STATUS, true)
//                }
                viewBinding.loginPatternLockView.isInputEnabled = true
            }
            if (TextUtils.equals(patternLockStatus, "N") || TextUtils.equals(
                    patternLockStatus,
                    "E"
                )
            ) {
//                SecuredPreferenceStoreManager.editAndApply { editor ->
//                    editor.putBoolean(Config.PREF_LOGIN_PATTERN_STATUS, false)
//                }
                viewBinding.loginPatternLockView.isInputEnabled = false
            }
        }

        if (!TextUtils.isEmpty(rspCode) && rspCode == QUERY_USER_RSP_CODE_UNAUTHORIZED) {
            Timber.d("Query user unauthorized")

            viewBinding.loginPatternLockView.isInputEnabled = false

            viewBinding.loginEditTextPassword.isEnabled = false
            viewBinding.loginEditTextPassword.inputType = InputType.TYPE_NULL

            viewBinding.loginEditTextCaptcha.isEnabled = false
            viewBinding.loginEditTextCaptcha.inputType = InputType.TYPE_NULL

            val alert = CommonDialog(this)
            alert.oneButtonInit(
                title = getString(R.string.login_alert_user_unauthorized_title),
                body = getString(R.string.login_alert_user_unauthorized_body),
                image = R.drawable.alert_1,
                text = getString(R.string.cancel),
                showButtons = true,
                canceledOnTouchOutside = true,
            )
            alert.show()
        }

        if (!TextUtils.isEmpty(rspCode) && rspCode == QUERY_USER_RSP_CODE_USER_NOT_FOUND) {
            Timber.d("Query user not found")
            val alert = CommonDialog(this)
            alert.twoButtonInit(
                title = getString(R.string.login_alert_user_not_found_title),
                body = getString(R.string.login_alert_user_not_found_body),
                image = R.drawable.alert_1,
                positiveText = getString(R.string.register_now),
                negativeText = getString(R.string.register_next),
                showButtons = true,
                canceledOnTouchOutside = true,
                positiveOnClick = {
                    toRegister()
                },
                negativeOnClick = {}
            )
            alert.show()
        }
    }


    private fun login() {
        if (TextUtils.isEmpty(viewBinding.loginEditTextPasswordPid.text.toString()) ||
            viewBinding.loginEditTextPasswordPid.text
                .toString().length != 10
        ) {
            viewBinding.loginLayoutPasswordPid.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningPid.visibility = View.VISIBLE
            return
        }

        if (TextUtils.isEmpty(viewBinding.loginEditTextPassword.text.toString())) {
            viewBinding.loginLayoutPasswordPassword.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningPassword.visibility = View.VISIBLE
            return
        }

        if (TextUtils.isEmpty(viewBinding.loginEditTextCaptcha.getText().toString())
            || genText != viewBinding.loginEditTextCaptcha.getText().toString().uppercase()
        ) {
            viewBinding.loginLayoutPasswordCaptcha.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningCaptcha.visibility = View.VISIBLE

            genText = genRandomNumbers()
            viewBinding.loginButtonCaptcha.text = genText
            return
        }
        if (viewBinding.loginTextViewWarningPid.isVisible) {
            return
        }

        // 這段換成新的API
//        val loginURL: String = EnvironmentManager.getEnvironmentConfig().cssoUrl + "login"
//        val loginParams = "SYS_ID=teamwalk" + "&" +
//                "appl_id=" + pid + "&" +
//                "appl_pwd=" + viewBinding.loginEditTextPassword.getText().toString() + "&" +
//                "service=teamwalk" + BuildConfig.BUILD_TYPE + "://loginsuccess"
//
//        val signInIntent = Intent()
//        signInIntent.putExtra("pid", pid)
//        signInIntent.putExtra("url", loginURL)
//        signInIntent.putExtra("params", loginParams)

        // 新API 拿Ticket 然後登入取JWT
        loginViewModel.getTicket(pid, viewBinding.loginEditTextPassword.text.toString().trim())
        observeOnLifeCycle(
            loginViewModel.ticketFlow.sharedFlow,
            unSubscribeOnComplete = true
        ) { ticketUrl ->
            val uri = ticketUrl.toUri()
            ticket = uri.getQueryParameter("ticket")

            if (!TextUtils.isEmpty(ticket)) {
                // 拿到ticket
                loginViewModel.login(ticket!!, Utils.getDeviceId(this))
            }
        }
    }

    private fun tabSettings() {
        viewBinding.password.setOnClickListener {
            viewBinding.password.setTextColor(ContextCompat.getColor(this, R.color.white))
            viewBinding.password.setBackgroundResource(R.drawable.tab_left_selector_filled)
            viewBinding.pattern.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            viewBinding.pattern.setBackgroundResource(R.drawable.tab_right_selector)
            viewBinding.patternContainer.visibility = View.GONE
            viewBinding.passwordContainer.visibility = View.VISIBLE

            position = 0
            SecuredPreferenceStoreManager.editAndApply {
                it.putInt(Config.SP_LOGIN_SEGMENT_CONTROL_POS, 0)
            }

            viewBinding.loginEditTextPasswordPid.imeOptions = EditorInfo.IME_ACTION_NEXT
        }
        viewBinding.pattern.setOnClickListener {
            viewBinding.password.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            viewBinding.password.setBackgroundResource(R.drawable.tab_left_selector)
            viewBinding.pattern.setTextColor(ContextCompat.getColor(this, R.color.white))
            viewBinding.pattern.setBackgroundResource(R.drawable.tab_right_selector_filled)
            viewBinding.patternContainer.visibility = View.VISIBLE
            viewBinding.passwordContainer.visibility = View.GONE

            position = 1
            SecuredPreferenceStoreManager.editAndApply {
                it.putInt(Config.SP_LOGIN_SEGMENT_CONTROL_POS, 1)
            }

            viewBinding.loginEditTextPasswordPid.imeOptions = EditorInfo.IME_ACTION_DONE
        }

        position = SecuredPreferenceStoreManager.getInt(Config.SP_LOGIN_SEGMENT_CONTROL_POS, 0)
        when (position) {
            0 -> viewBinding.password.performClick()
            1 -> viewBinding.pattern.performClick()
        }
    }

    private fun toRegister() {
        val intent = CSSOWebViewActivity.register(this)
        startActivity(intent)
    }

    private fun toForgetPassword() {
        val intent = CSSOWebViewActivity.forgetPassword(this)
        startActivity(intent)
    }

    private fun setSpannable(textView: TextView, text: String, color: Int, onClick: () -> Unit) {
        val ss = SpannableString(text)
        val cs: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(color)
            }

            override fun onClick(widget: View) {
                onClick()
            }
        }

        ss.setSpan(cs, 0, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = ss
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun genRandomNumbers(): String {
        val sb = StringBuilder()
        for (i in 0..3) {
            val secureRandom = SecureRandom()
            val randomInt = secureRandom.nextInt('Z'.code - 'A'.code + 1)
            val random = ('A'.code + randomInt).toChar()
            //            char random = (char)((int)'A' + Math.random() * ((int)'Z' - (int)'A' + 1));
            sb.append(random)
        }
        return sb.toString()
    }

    override fun onStop() {
        super.onStop()

        // 看起來只要關掉螢幕就將此頁面關閉
        finish()
    }
}