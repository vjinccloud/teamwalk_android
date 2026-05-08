package com.taiwanlife.teamwalk.ui.login

import android.R.id.input
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
import android.app.AlertDialog
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
import com.taiwanlife.teamwalk.utils.isVersionLessThan
import com.taiwanlife.teamwalk.utils.MyWebChromeClient
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
import androidx.core.widget.addTextChangedListener
import androidx.savedstate.serialization.saved
import com.google.gson.Gson
import com.taiwanlife.teamwalk.utils.AppUuidManager
import timber.log.Timber

class LoginActivity : BaseActivity<ActivityLoginBinding>({ ActivityLoginBinding.inflate(it) }) {

    companion object {
        const val QUERY_PARAM_SERVICE_ID = "serviceId"
        const val QUERY_PARAM_TICKET = "ticket"
    }

    override val statusBarColor: Int = R.color.white

    // 登入流程：API 通常很快，立刻 show loading 會閃一下就消失（user 來不及看到）。
    // 改延遲 show，期間若 API 已回 → 根本不顯示 → 把 loading 留給後面 MainActivity 接力顯示
    override val showLoadingWithDelay: Boolean = true

    private val loginViewModel: LoginViewModel by viewModel()

    private var position = 0
    private var isRememberMe: Boolean = false
    private var fromFocusChange: Boolean = false
    private var pid = CharArray(10)
    private var ticketCallback: (ticket: String) -> Unit = {}
//    private lateinit var pidTextWatcher: PidTextWatcher
//    private var changed = false

    //    private var genText: String = ""
    private lateinit var currentCaptchaResult: CaptchaResult
    private var isLoginProcess = false

    override fun onLastCreateBaseActivity(
        view: View, savedInstanceState: Bundle?
    ) {
        DeviceUtil.setFlagSecure(this)

        observeOnLifeCycle(loginViewModel.loginFlow, onError = {
            viewBinding.loginPatternLockView.clearPattern()
            setLoginUI(false)
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

            setLoginUI(false)
            setResult(RESULT_OK)
            finish()
        }
        observeOnLifeCycle(loginViewModel.systemParamFlow) { systemParamResponse ->
            systemParamResponse.forceUpdateVerAndroid?.let { ver ->
                if (BuildConfig.VERSION_NAME.isVersionLessThan(ver)) {
                    // 需要版本更新
                    systemParamResponse.androidIsForced?.let { forced ->
                        if (!(isFinishing || isDestroyed)) {
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

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName ?: ""
            // 非正式環境在版號後面標示環境別，方便 QA / 偵錯
            viewBinding.loginBuildAppv.text = if (BuildConfig.BUILD_TYPE == "release") {
                versionName
            } else {
                "$versionName (${BuildConfig.BUILD_TYPE.uppercase()})"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var showSecurity =
            SecuredPreferenceStoreManager.getBoolean(Config.SP_SHOW_SECURITY_ALERT_FIRST_TIME, true)
        if (showSecurity) {
            if (isFinishing || isDestroyed) return

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

//        pidTextWatcher = PidTextWatcher(
//            viewBinding.loginEditTextPid, pid, ::validPid
//        )
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

        if (isFinishing || isDestroyed) return

        val emulatorResult = SecurityCheckManager.runShutdownCheck(this)
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
        // 0003005: CSSO 透過 user-agent 判斷是否為 teamwalk app
        webSettings.userAgentString = webSettings.userAgentString + "/env=taiwanlife_teamwalk_app"
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // 0003003: 不受系統字級影響，避免大字級導致跑版
        webSettings.textZoom = 100


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
                                SecuredPreferenceStoreManager.simpleEditAndApply(
                                    Config.SP_CASTGC,
                                    castGC
                                )
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
                                    EnvironmentManager.getEnvironmentConfig().loginSuccessRedirectUrl,
                                    String(pid),
                                    ticket,
                                    Utils.getDeviceId(this@LoginActivity),
                                    isRememberMe
                                )
                            )
                            val intent =
                                CSSOWebViewActivity.notifyChangePassword(this@LoginActivity)
                            startActivity(intent)
                            setLoginUI(false)
                        } else {
                            ticketCallback(ticket)
                        }
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
                setLoginUI(false)
                // 確保錯誤是針對主框架的請求 (isForMainFrame)
                if (request?.isForMainFrame == true) {
                    if (isFinishing || isDestroyed) return

                    val description = error?.description.toString()
                    val errorCode = error?.errorCode ?: -1

                    // 用系統版 AlertDialog + try-catch，避免 1dp×1dp webview 環境
                    // 跟 activity transition 競態時 BadTokenException 閃退
                    try {
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
                            .setPositiveButton(R.string.confirm1) { _, _ -> }
                            .setCancelable(true)
                            .show()
                    } catch (e: Exception) {
                        Timber.e(e, "onReceivedError show dialog failed")
                    }
                }
            }

        }
        val myWebChromeClient = MyWebChromeClient(this)
        myWebChromeClient.setAlertCallback {
            viewBinding.loginPatternLockView.clearPattern()
            setLoginUI(false)
        }
        myWebChromeClient.setConfirmCallback {
            viewBinding.loginPatternLockView.clearPattern()
            setLoginUI(false)
        }
        viewBinding.webview.webChromeClient = myWebChromeClient
        ticketCallback = { ticket ->
            loginViewModel.login(
                EnvironmentManager.getEnvironmentConfig().loginSuccessRedirectUrl,
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
        viewBinding.loginEditTextPid.addTextChangedListener { editable ->
            if (fromFocusChange) {
                fromFocusChange = false
                return@addTextChangedListener
            }
            val input = editable?.toString()?.trim() ?: return@addTextChangedListener

            // 更改過即使用現在的輸入值
            Arrays.fill(pid, '\u0000')
            val copyLength = minOf(input.length, pid.size)
            input.toCharArray(pid, 0, 0, copyLength)

            if (input.length == 10) {
                validPid()
            }
        }
//        viewBinding.loginEditTextPasswordPid.addTextChangedListener(pidTextWatcher)
        if (isRememberMe) {
            run {
                SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_REMEMBER_PID, "")
                    .toCharArray(pid, 0)
                safeMask(pid)
                // 離開後，savedPid 立即失去引用，標記為可回收
            }
        } else {
            Arrays.fill(pid, '\u0000')
        }
        viewBinding.loginEditTextPid.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    safeMask(pid)
                }
            }
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
                val fid = AppUuidManager.getOrCreate()
                if (pid.count { it != '\u0000' } != 10) {
                    viewBinding.loginLayoutPid.setBackgroundColor(getColor(R.color.colorError))
                    viewBinding.loginLayoutPid.visibility
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

    private fun validPid() {
        // 到這邊表示pid 為10碼
        viewBinding.loginTextViewWarningPid.visibility = View.GONE
        viewBinding.loginLayoutPid.background = null

        viewBinding.loginPatternLockView.isInputEnabled = true
    }


    private fun login() {
        if (pid.count { it != '\u0000' } != 10) {
            viewBinding.loginLayoutPid.setBackgroundColor(getColor(R.color.colorError))
            viewBinding.loginTextViewWarningPid.visibility = View.VISIBLE
            return
        }
//        if (viewBinding.loginEditTextPid.text.isEmpty() || viewBinding.loginEditTextPid.text.length != 10) {
//            viewBinding.loginLayoutPid.setBackgroundColor(getColor(R.color.colorError))
//            viewBinding.loginTextViewWarningPid.visibility = View.VISIBLE
//            return
//        }

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

        if(isLoginProcess) return

        setLoginUI(true)

        when (BuildConfig.BUILD_TYPE) {
            "debug" -> {
                // 用網頁打比照舊版 等同下面註解的API
                getPWTicketFromWebview(EnvironmentManager.getEnvironmentConfig().apiUrl + "mock/csso")
//                getPWTicketFromWebview(EnvironmentManager.getEnvironmentConfig().cssoUrl + "login")
            }

            else -> {
                getPWTicketFromWebview(EnvironmentManager.getEnvironmentConfig().cssoUrl + "login")
            }
        }
    }

    private fun setLoginUI(enable: Boolean) {
        isLoginProcess = enable
        if(isLoginProcess) {
            viewBinding.loginButton.background = ContextCompat.getDrawable(this, R.drawable.button_style_pressed)
            viewBinding.loginButton.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
        } else {
            viewBinding.loginButton.background = ContextCompat.getDrawable(this, R.drawable.button_style)
            viewBinding.loginButton.setTextColor(ContextCompat.getColor(this, R.color.colorLinkText))
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

            viewBinding.loginEditTextPid.imeOptions = EditorInfo.IME_ACTION_NEXT
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

            viewBinding.loginEditTextPid.imeOptions = EditorInfo.IME_ACTION_DONE
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
        viewBinding.loginEditTextPid.text?.clear()

//        pidTextWatcher.clear()
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
            SecuredPreferenceStoreManager.simpleEditAndApply(
                Config.SP_CHANGE_PARAMS,
                Gson().toJson(changeParams)
            )
            return true

        } catch (e: Exception) {
            false
        }
    }

    fun safeMask(charArray: CharArray) {
        fromFocusChange = true
        val sb = StringBuilder()
        charArray.forEachIndexed { index, ch ->
            if(ch == '\u0000') return@forEachIndexed

            if (index >= 0 && index < 3) {
                sb.append(ch)
            } else if (index >= 3 && index < 8) {
                sb.append("*")
            } else {
                sb.append(ch)
            }
        }
        viewBinding.loginEditTextPid.setText(sb.toString())
    }
}