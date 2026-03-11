package com.taiwanlife.teamwalk.ui.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityLoginBinding
import com.taiwanlife.teamwalk.java_utils.DeviceUtil
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog
import com.taiwanlife.teamwalk.utils.CustomTextWatcher
import com.taiwanlife.teamwalk.utils.MyWebChromeClient
import com.taiwanlife.teamwalk.utils.PidTextWatcher
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.SecurityCheckManager
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.Utils.openPlayStoreAndExit
import com.taiwanlife.teamwalk.utils.enableToBoolean
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Arrays
import java.util.Locale
import androidx.core.net.toUri
import com.google.gson.Gson

class LoginActivity : BaseActivity<ActivityLoginBinding>({ ActivityLoginBinding.inflate(it) }) {

    companion object {
        const val QUERY_PARAM_SERVICE_ID = "serviceId"
        const val QUERY_PARAM_TICKET = "ticket"
    }

    override val statusBarColor: Int = R.color.white

    private val loginViewModel: LoginViewModel by viewModel()

    private var position = 0
    private var isRememberMe: Boolean = false
    private var pid = CharArray(10)
    private var ticketCallback: (ticket: String) -> Unit = {}
    private lateinit var pidTextWatcher: PidTextWatcher

    //    private var genText: String = ""
    private lateinit var currentCaptchaResult: CaptchaResult

    override fun onLastCreateBaseActivity(
        view: View, savedInstanceState: Bundle?
    ) {
        DeviceUtil.setFlagSecure(this)

        observeOnLifeCycle(loginViewModel.loginFlow, onError = {
            viewBinding.loginPatternLockView.clearPattern()
        }) { loginResponse ->
            SecuredPreferenceStoreManager.editAndApply {
                it.putBoolean(Config.SP_LOGIN_AUTH, true)
//                it.putBoolean(Config.PREF_LOGIN_AUTH, true)
//                it.putString(Config.PREF_LOGIN_TICKET, ticket!!)
//                it.putString(Config.PREF_LOGIN_USERNAME, pid)

                if (isRememberMe) {
//                    it.putString(Config.PREF_LOGIN_PID, pid)
                    it.putString(Config.SP_LOGIN_REMEMBER_PID, String(pid))
                } else {
//                    it.putString(Config.PREF_LOGIN_PID, "")
                    it.putString(Config.SP_LOGIN_REMEMBER_PID, "")
                }
                it.putString(Config.SP_PID, String(pid))

                loginResponse.let { loginResponse ->
                    it.putString(Config.SP_LOGIN_JWT, loginResponse.token)
                }
            }

            setResult(RESULT_OK)
            finish()
        }
        observeOnLifeCycle(loginViewModel.systemParamFlow) { systemParamResponse ->
            systemParamResponse.forceUpdateVerAndroid?.let { ver ->
                if (!BuildConfig.VERSION_NAME.startsWith(ver)) {
                    // 需要版本更新
                    systemParamResponse.androidIsForced?.let { forced ->
                        if (forced.enableToBoolean()) {
                            // 強制版本更新
                            CommonDialog(this).apply {
                                oneButtonInit(
                                    getString(R.string.main_update_title),
                                    getString(R.string.main_force_update),
                                    R.drawable.alert_1,
                                    showButtons = true,
                                    canceledOnTouchOutside = false,
                                    text = getString(R.string.main_force_update_confirm),
                                    onClick = {
                                        openPlayStoreAndExit(this@LoginActivity)
                                    },
                                )
                                setCancelable(false)
                            }.show()
                        } else {
                            // 非強制版本更新
                            CommonDialog(this).apply {
                                twoButtonInit(
                                    getString(R.string.main_update_title),
                                    getString(R.string.main_recommend_update),
                                    R.drawable.alert_1,
                                    showButtons = true,
                                    canceledOnTouchOutside = false,
                                    positiveText = getString(R.string.main_force_update_confirm),
                                    positiveOnClick = {
                                        openPlayStoreAndExit(this@LoginActivity)
                                    },
                                    negativeText = getString(R.string.close),
                                    negativeOnClick = {

                                    }
                                )
                                setCancelable(false)
                            }.show()
                        }
                    }
                }
            }
        }
//        observeOnLifeCycle(loginViewModel.patternFlow) { ticketUrl ->
//            val uri = ticketUrl.toUri()
//            ticket = uri.getQueryParameter(QUERY_PARAM_TICKET)
//
//            if (!TextUtils.isEmpty(ticket)) {
//                // 拿到ticket
//                loginViewModel.login(pid, ticket!!, Utils.getDeviceId(this))
//            }
//
//        }

        isRememberMe = SecuredPreferenceStoreManager.getBoolean(Config.SP_LOGIN_REMEMBER_ME, false)

        if (isRememberMe) {
            run {
                val savedPid =
                    SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_REMEMBER_PID, "")
                if (savedPid.isNotEmpty()) {
                    savedPid.toCharArray(pid, 0)
                }
                // 離開後，savedPid 立即失去引用，標記為可回收
            }
        } else {
            Arrays.fill(pid, '\u0000')
        }

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            viewBinding.loginBuildAppv.text = packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        var uuid = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_UUID, "")
        var showSecurity =
            SecuredPreferenceStoreManager.getBoolean(Config.SP_SHOW_SECURITY_ALERT_FIRST_TIME, true)
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
                text = getString(R.string.confirm2),
                onClick = {
                    SecuredPreferenceStoreManager.editAndApply {
                        it.putBoolean(Config.SP_SHOW_SECURITY_ALERT_FIRST_TIME, false)
                    }
                })
            commonDialog.show()
        }

        pidTextWatcher = PidTextWatcher(
            viewBinding.loginEditTextPasswordPid, pid, ::validPid
        )
        setWebview()
        setPidUI()
        setPasswordUI()
        setCaptcha()
        setPatterLock()
        viewBinding.loginButton.setOnClickListener {
            login()
        }


        tabSettings()

        // 記住我
        viewBinding.loginCheckBoxRememberMe.isChecked = isRememberMe
        viewBinding.loginCheckBoxRememberMe.setOnCheckedChangeListener { button, isChecked ->
            isRememberMe = isChecked

            SecuredPreferenceStoreManager.editAndApply {
                it.putBoolean(Config.SP_LOGIN_REMEMBER_ME, isRememberMe)
            }
        }

        viewBinding.loginSignupTextView.apply {
            // 加上底線
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG

            setOnClickListener {
                toRegister()
            }
        }
        viewBinding.loginForgetPassTextView.apply {
            // 加上底線
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG

            setOnClickListener {
                toForgetPassword()
            }
        }

        loginViewModel.getSysParam()
    }

    override fun onResume() {
        super.onResume()

        val emulatorResult = SecurityCheckManager.runEmulatorCheck(this)
        if (!emulatorResult.passed) {
            getAlertDialog(
                context = this,
                message = emulatorResult.errorMessage ?: "",
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.confirm1),
                positiveOnClick = {
                    finishAffinity()
                }
            )
            return
        }
        val result = SecurityCheckManager.runSecurityCheck(this)
        if (!result.passed) {
            getAlertDialog(
                context = this,
                message = result.errorMessage ?: "",
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {}
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebview() {
        val webSettings = viewBinding.webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true


        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true


        // Enable pinch to zoom without the zoom buttons
        webSettings.builtInZoomControls = false

        // Hide the zoom controls for HONEYCOMB+
        webSettings.displayZoomControls = false

        viewBinding.webview.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                url: String
//            ): Boolean {
//                return tryOverrideUrlLoading(url)
//            }
//
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest
//            ): Boolean {
//                return tryOverrideUrlLoading(request.url.toString())
//            }
//
//            private fun tryOverrideUrlLoading(url: String):Boolean {
//                if (url.isNotEmpty() && url.startsWith("teamwalk")) {
//                    return true
//                }
//                return false
//            }

            override fun onPageStarted(
                view: WebView?, url: String, favicon: Bitmap?
            ) {
                viewBinding.url.text = url

                if (url.isNotEmpty()) {
                    val ticket = Utils.extractTicketFromUrl(url, QUERY_PARAM_TICKET)
                    if (!ticket.isNullOrEmpty()) {
//                      this.debugToast("嘗試取得Cookie url - ${EnvironmentManager.getEnvironmentConfig().cssoUrl + "login"}")
                        val cookieString = CookieManager.getInstance()
                            .getCookie(EnvironmentManager.getEnvironmentConfig().cssoUrl + "login")
                        if (cookieString != null) {
//                          this.debugToast("嘗試找CASTGC - $cookieString")
                            val castGC = extractCastgcValueSplit(cookieString)
                            if (!castGC.isNullOrEmpty()) {
                                SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_CASTGC, castGC)
                            } else {
//                          this.debugToast("找不到CASTGC")
                            }
                        } else {
//                          this.debugToast("沒有取得CookieString")
                        }

                        if (url.contains(Config.CHANGE_PATH) && extractChangeParams(url)) {
                            // 需要更換密碼
                            SecuredPreferenceStoreManager.simpleEditAndApply(
                                Config.SP_LOG_REQUEST, loginViewModel.getLogRequest(
                                    getString(R.string.redirect_scheme),
                                    String(pid),
                                    ticket,
                                    Utils.getDeviceId(this@LoginActivity),
                                    isRememberMe
                                )
                            )
                            val intent =
                                CSSOWebViewActivity.notifyChangePassword(this@LoginActivity)
                            startActivity(intent)
                        } else {
                            ticketCallback(ticket)
                        }

                        viewBinding.webview.loadUrl("about:blank")
                        return
                    }
                }

                super.onPageStarted(view, url, favicon)
            }

            @Override
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                viewBinding.loginPatternLockView.clearPattern()
                if (request?.url?.scheme?.startsWith(Config.WEBVIEW_CALLBACK_SCHEME) == true) {
                    return
                }
                // 確保錯誤是針對主框架的請求 (isForMainFrame)
                if (request?.isForMainFrame == true) {
                    val description = error?.description.toString()
                    val errorCode = error?.errorCode ?: -1

                    AlertDialog.Builder(this@LoginActivity)
                        .setTitle(
                            String.format(
                                Locale.getDefault(),
                                getString(R.string.webview_error_title),
                                errorCode.toString()
                            )
                        )
                        .setMessage(
                            String.format(
                                Locale.getDefault(),
                                getString(R.string.webview_error_message),
                                description
                            )
                        )
                        .setPositiveButton(R.string.confirm1) { dialog, _ ->

                        }
                        .setCancelable(true)
                        .show()
                }
            }

        }
        val myWebChromeClient = MyWebChromeClient(this)
        myWebChromeClient.setAlertCallback { viewBinding.loginPatternLockView.clearPattern() }
        myWebChromeClient.setConfirmCallback { viewBinding.loginPatternLockView.clearPattern() }
        viewBinding.webview.webChromeClient = myWebChromeClient
        ticketCallback = { ticket ->
            loginViewModel.login(
                getString(R.string.redirect_scheme),
                String(pid),
                ticket,
                Utils.getDeviceId(this)
            )
        }
    }

    private fun extractCastgcValueSplit(input: String): String? {
        val parts = input.split(';')
        val castgcEntry = parts.find { it.trim().startsWith("CASTGC=") }
        return castgcEntry?.substringAfter("CASTGC=")?.trim()
    }

    private fun setPidUI() {
        viewBinding.loginEditTextPasswordPid.addTextChangedListener(pidTextWatcher)
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

        val reload = { _: View ->
//            genText = genRandomNumbers()
//            viewBinding.loginButtonCaptcha.text = genText
            currentCaptchaResult = CaptchaGenerator.generateCaptchaBitmap()
            viewBinding.loginButtonCaptcha.setImageBitmap(currentCaptchaResult.bitmap)
        }
        viewBinding.loginButtonCaptcha.setOnClickListener(reload)
        viewBinding.loginReloadCaptcha.setOnClickListener(reload)

//        genText = genRandomNumbers()
//        viewBinding.loginButtonCaptcha.text = genText
        currentCaptchaResult = CaptchaGenerator.generateCaptchaBitmap()
        viewBinding.loginButtonCaptcha.setImageBitmap(currentCaptchaResult.bitmap)
    }

    private fun setPatterLock() {

        viewBinding.loginPatternLockView.isInStealthMode = false
        viewBinding.loginPatternLockView.isInputEnabled =
            isRememberMe && pid.count { it != '\u0000' } == 10

        viewBinding.loginPatternToggleStealthModeButton.setOnClickListener {
            viewBinding.loginPatternLockView.isInStealthMode =
                !viewBinding.loginPatternLockView.isInStealthMode
            if (!viewBinding.loginPatternLockView.isInStealthMode) {
                viewBinding.loginPatternToggleStealthModeButton.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.visibility, 0
                )
                viewBinding.loginPatternToggleStealthModeButton.text =
                    getString(R.string.login_pattern_normal_mode)
            } else {
                viewBinding.loginPatternToggleStealthModeButton.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.visibility_off, 0
                )
                viewBinding.loginPatternToggleStealthModeButton.text =
                    getString(R.string.login_pattern_stealth_mode)
            }
        }

        viewBinding.loginPatternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {}

            override fun onProgress(progressPattern: List<PatternLockView.Dot?>?) {}

            override fun onComplete(pattern: List<PatternLockView.Dot>) {
                val fid = SecuredPreferenceStoreManager.getString(
                    Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID, ""
                )
                if (pid.count { it != '\u0000' } != 10) {
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

                    when (BuildConfig.BUILD_TYPE) {
                        "debug" -> {
                            // 用網頁打比照舊版 等同下面註解的API
                            getPatternTicketFromWebview(
                                pattern,
                                fid,
                                EnvironmentManager.getEnvironmentConfig().apiUrl + "mock/csso"
                            )
                        }

                        else -> {
                            getPatternTicketFromWebview(
                                pattern,
                                fid,
                                EnvironmentManager.getEnvironmentConfig().cssoUrl + "patternLogin"
                            )
                        }
                    }
//                    loginViewModel.patternLogin(pid, patternPath)
//                    // 新API 拿Ticket 然後登入取JWT
//                    loginViewModel.getTicket(pid, patternPath)
//                    observeOnLifeCycle(
//                        loginViewModel.ticketFlow.sharedFlow,
//                        unSubscribeOnComplete = true
//                    ) { ticketUrl ->
//                        val uri = ticketUrl.toUri()
//                        ticket = uri.getQueryParameter(QUERY_PARAM_TICKET)
//
//                        if (!TextUtils.isEmpty(ticket)) {
//                            // 拿到ticket
//                            loginViewModel.patternLogin(pid, patternPath)
//                        }
////                        loginViewModel.login(
////                            ticket!!,
////                            Utils.getDeviceId(this@LoginActivity)
////                        )
//                    }
                }
                viewBinding.loginPatternLockView.postDelayed({
                    viewBinding.loginPatternLockView.clearPattern()
                }, 1000L)
            }

            override fun onCleared() {}
        })
    }

    private fun validPid(pidText: String) {
        // 到這邊表示pid OK
        pidText.toCharArray(pid, 0)

        viewBinding.loginTextViewWarningPid.visibility = View.GONE
        viewBinding.loginLayoutPasswordPid.background = null

        viewBinding.loginPatternLockView.isInputEnabled = true
    }


    private fun login() {
        if (viewBinding.loginEditTextPasswordPid.text.isEmpty() || viewBinding.loginEditTextPasswordPid.text.length != 10) {
            viewBinding.loginLayoutPasswordPid.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningPid.visibility = View.VISIBLE
            return
        }

        if (viewBinding.loginEditTextPassword.text.isEmpty()) {
            viewBinding.loginLayoutPasswordPassword.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningPassword.visibility = View.VISIBLE
            return
        }
        if (viewBinding.loginEditTextPassword.text.isEmpty()) {
            viewBinding.loginLayoutPasswordPassword.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningPassword.visibility = View.VISIBLE
            return
        }

        if (viewBinding.loginEditTextCaptcha.text.isEmpty() || currentCaptchaResult.code.uppercase() != viewBinding.loginEditTextCaptcha.text.toString()
                .uppercase()
        ) {
            viewBinding.loginLayoutPasswordCaptcha.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningCaptcha.visibility = View.VISIBLE

//            genText = genRandomNumbers()
//            viewBinding.loginButtonCaptcha.text = genText
            currentCaptchaResult = CaptchaGenerator.generateCaptchaBitmap()
            viewBinding.loginButtonCaptcha.setImageBitmap(currentCaptchaResult.bitmap)
            return
        }
        if (viewBinding.loginTextViewWarningPid.isVisible) {
            return
        }

        when (BuildConfig.BUILD_TYPE) {
            "debug" -> {
                // 用網頁打比照舊版 等同下面註解的API
                getPWTicketFromWebview(EnvironmentManager.getEnvironmentConfig().apiUrl + "mock/csso")
            }

            else -> {
                getPWTicketFromWebview(EnvironmentManager.getEnvironmentConfig().cssoUrl + "login")
            }
        }
    }

    private fun getPWTicketFromWebview(loginURL: String) {
        val sb = StringBuilder()
        sb.append("SYS_ID=teamwalk")
        sb.append("&appl_id=")
        sb.append(pid)
        sb.append("&appl_pwd=")
        sb.append(viewBinding.loginEditTextPassword.text)
//        sb.append("&service=${getString(R.string.redirect_scheme)}://loginsuccess") // teamwalkuat://loginsuccess
        sb.append("&service=${getString(R.string.login_success_redirect_url)}") // https://teamwalk2uat.taiwanlife.com/loginsuccess

        val params = sb.toString()
//        val loginParams =
//            "SYS_ID=teamwalk&appl_id=$pid&appl_pwd=" + viewBinding.loginEditTextPassword.text
//                .toString() + "&" + "service=${getString(R.string.redirect_scheme)}://loginsuccess"

        viewBinding.webview.postUrl(loginURL, params.toByteArray())

        for (i in 0 until sb.length) {
            sb.setCharAt(i, '\u0000') // 逐個字元覆寫為 0
        }
        sb.setLength(0)
    }

    private fun getPatternTicketFromWebview(
        pattern: List<PatternLockView.Dot>,
        fid: String,
        loginURL: String
    ) {
        val patternPath = Utils.patternToSha256(
            viewBinding.loginPatternLockView, pattern.toMutableList(), fid
        )

        val sb = StringBuilder()
        sb.append("SYS_ID=teamwalk")
        sb.append("&userId=")
        sb.append(pid)
        sb.append("&pattern_path=")
        sb.append(patternPath)
//        sb.append("&service=${getString(R.string.redirect_scheme)}://loginsuccess") // teamwalkuat://loginsuccess
        sb.append("&service=${getString(R.string.login_success_redirect_url)}") // https://teamwalk2uat.taiwanlife.com/loginsuccess

//        val loginParams =
//            "SYS_ID=teamwalk&userId=$pid&pattern_path=" + patternPath + "&" + "service=${getString(R.string.redirect_scheme)}://loginsuccess"

        viewBinding.webview.postUrl(loginURL, sb.toString().toByteArray())


        for (i in 0 until sb.length) {
            sb.setCharAt(i, '\u0000') // 逐個字元覆寫為 0
        }
        sb.setLength(0)
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

    override fun onStop() {
        super.onStop()

        // 看起來只要關掉螢幕就將此頁面關閉
        viewBinding.loginEditTextPassword.text?.clear()
        viewBinding.loginEditTextCaptcha.text?.clear()
        viewBinding.loginEditTextPasswordPid.text?.clear()

        pidTextWatcher.clear()
        Arrays.fill(pid, '\u0000')
        finish()
    }

    fun extractChangeParams(url: String): Boolean {
        return try {
            val uri = url.toUri()

            // 確認該網址於我們的白名單內，為可信任之來源
            if (uri.scheme != "https" || !Config.ALLOW_WEBVIEW_DOMAIN.contains(uri.host)) {
                return false
            }

            // 將需要傳入的 serviceId/ticket 取出
            val rawServiceId = uri.getQueryParameter(QUERY_PARAM_SERVICE_ID) ?: return false
            val ticket = uri.getQueryParameter(QUERY_PARAM_TICKET) ?: return false

            val decodedServiceId = Uri.decode(rawServiceId)


            val changeParams = ChangeParams(
                serviceId = decodedServiceId,
                ticket = ticket
            )

            // 將此參數加密後存入本地
            SecuredPreferenceStoreManager.simpleEditAndApply(Config.SP_CHANGE_PARAMS, Gson().toJson(changeParams))
            return true

        } catch (e: Exception) {
            false
        }
    }
}