package com.taiwanlife.teamwalk.ui.main

import android.Manifest
import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import com.andrognito.patternlockview.PatternLockView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.MyApplication.Companion.context
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityMainBinding
import com.taiwanlife.teamwalk.java_utils.CelebrusCSAUtil
import com.taiwanlife.teamwalk.java_utils.DeviceUtil
import com.taiwanlife.teamwalk.java_utils.SensitiveDataUtil
import com.taiwanlife.teamwalk.remote.HealthConnectRepository
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import com.taiwanlife.teamwalk.test.TestActivity
import com.taiwanlife.teamwalk.ui.common.CommonDialog
import com.taiwanlife.teamwalk.ui.common.FitbitViewModel
import com.taiwanlife.teamwalk.ui.common.GarminViewModel
import com.taiwanlife.teamwalk.ui.common.HealthConnectViewModel
import com.taiwanlife.teamwalk.ui.common.model.FitbitData
import com.taiwanlife.teamwalk.ui.common.model.FitbitModel
import com.taiwanlife.teamwalk.ui.common.model.GarminData
import com.taiwanlife.teamwalk.ui.common.model.GarminModel
import com.taiwanlife.teamwalk.ui.common.model.SyncHealthDataModel
import com.taiwanlife.teamwalk.ui.login.LoginActivity
import com.taiwanlife.teamwalk.ui.main.HostTypes.HOME
import com.taiwanlife.teamwalk.ui.main.HostTypes.LOGIN
import com.taiwanlife.teamwalk.ui.main.HostTypes.LOGIN_FAILURE
import com.taiwanlife.teamwalk.ui.main.HostTypes.LOGIN_SUCCESS
import com.taiwanlife.teamwalk.ui.main.HostTypes.ONBOARDING
import com.taiwanlife.teamwalk.ui.main.HostTypes.USER_INFO
import com.taiwanlife.teamwalk.ui.main.webview.MyWebAppInterface
import com.taiwanlife.teamwalk.ui.main.webview.MyWebView
import com.taiwanlife.teamwalk.ui.onboarding.PromoteActivity
import com.taiwanlife.teamwalk.ui.pattern.PatternSetupActivity
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog
import com.taiwanlife.teamwalk.utils.BindingManager
import com.taiwanlife.teamwalk.utils.DeviceType
import com.taiwanlife.teamwalk.utils.DeviceType.FITBIT
import com.taiwanlife.teamwalk.utils.DeviceType.GARMIN
import com.taiwanlife.teamwalk.utils.DeviceType.HEALTH_CONNECT
import com.taiwanlife.teamwalk.utils.DeviceType.NONE
import com.taiwanlife.teamwalk.utils.HealthConnectHelper
import com.taiwanlife.teamwalk.utils.MyNotificationManager
import com.taiwanlife.teamwalk.utils.PermissionManager
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import com.taiwanlife.teamwalk.utils.ShareUtil
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.enableToBoolean
import com.taiwanlife.teamwalk.utils.enableToString
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.quoteJS
import com.taiwanlife.teamwalk.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Locale
import kotlin.random.Random

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }),
    ProviderInstaller.ProviderInstallListener,
    MyWebAppInterface.AsyncCallbacks, MyWebView.WebviewLoadingCallback {

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 4
        const val GOOGLE_ERROR_DIALOG_REQUEST_CODE = 4

        private const val QUERY_PARAM_TICKET = "ticket"
        private const val KEY_PID = "pid"
    }

    private var badgeCount = 0;
    private lateinit var myNotificationManager: MyNotificationManager
    private val permissionManager = PermissionManager(this)
    private val mainViewModel: MainViewModel by viewModel()
    private val fitbitViewModel: FitbitViewModel by viewModel()
    private val garminViewModel: GarminViewModel by viewModel()
    private var healthConnectViewModel: HealthConnectViewModel? = null
    private val healthConnectHelper = HealthConnectHelper(this, this)
    private lateinit var bindingManager: BindingManager

    // 檢查Play商店功能參數
    private var retryProviderInstall: Boolean = false

    // Google GSO 拿到的登入狀態 以前叫做authCode
    private var googleAuthCode: String? = null

    // 跟資安有關的參數
    private var isKnowsDeviceSecure = false
    private var isKnowsReverseToolRunning = false
    private var isKnowsCovered = false

    private var clearCache: Boolean? = null

    // 提供登入的LoginActivity之資料回傳
    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                debugToast(R.string.login_success)

                // 登入完畢 取得使用者資訊
                mainViewModel.getLanding()


                // 現在都是打API 這邊不用處理了
//                // 取得回傳資料處理
//                result.data?.let { data ->
//                    pid = data.getStringExtra(KEY_PID)
//                    val url = data.getStringExtra(KEY_URL)
//                    val postData = data.getStringExtra(KEY_PARAMS)?.toByteArray()
//                    if (url != null && URLUtil.isNetworkUrl(url) && postData != null) {
//                        viewBinding.webView.postUrl(url, postData)
//                    }
//                }
                // 以前最後webview會走到 下面的 loginSuccess
//                loginSuccess()
            }
        }

    // 提供變更圖形密碼 PatternSetupActivity之成果回傳
    private val patternLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            debugToast(R.string.change_success)

            if (result.resultCode == RESULT_OK) {
                val changed = result.data?.getBooleanExtra(
                    PatternSetupActivity.KEY_IS_GRAPHICAL_LOGIN_CHANGED,
                    false
                )
                val isGraphicalLoginSet = result.data?.getBooleanExtra(
                    PatternSetupActivity.KEY_IS_GRAPHICAL_LOGIN_SET,
                    false
                )
                // 如果有變更圖形修改的狀況 就把最新的綁定圖形結果回傳
                if (changed == true && isGraphicalLoginSet != null) {
                    postEvent(
                        EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                        isGraphicalLoginSet.enableToString().quoteJS()
                    )
                } else {
                    // 如果沒有改變 就將使用者現在的狀況回傳
                    val userInfoString =
                        SecuredPreferenceStoreManager.getString(Config.SP_USER_INFO, "")
                    val userInfoResponse =
                        getGson().fromJson(userInfoString, UserInfoResponse::class.java)

                    if (userInfoString.isNotEmpty() && userInfoResponse.bindingCaptcha != null) {
                        postEvent(
                            EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                            userInfoResponse.bindingCaptcha.enableToString().quoteJS()
                        )
                    } else {
                        // 防呆傳入N 如果有登入不該走到這裡
                        postEvent(
                            EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                            false.enableToString().quoteJS()
                        )
                    }
                }
            }
        }


    private var tempFileData: String? = null
    private var tempFileName: String? = null
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            if (tempFileData == null || tempFileName == null) return@registerForActivityResult

            mainViewModel.startToWriteFile(contentResolver, uri, tempFileName!!) {
                tempFileData = null
                tempFileName = null
            }
        }
    }

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
                filePathCallback?.onReceiveValue(uris)
            } else {
                filePathCallback?.onReceiveValue(null)
            }
            filePathCallback = null
        }

    override fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    ) {
        // 設置安全Utils
        DeviceUtil.setFlagSecure(this)

        myNotificationManager = MyNotificationManager(this)

        if (healthConnectHelper.availableStatusFlow()) {
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            val healthConnectRepository = HealthConnectRepository(healthConnectClient)
            healthConnectViewModel = HealthConnectViewModel(healthConnectRepository)
        }
        bindingManager = BindingManager(
            this,
            garminViewModel,
            fitbitViewModel,
            healthConnectHelper,
            ::sameDeviceCallback,
            ::bindingRemoved,
            ::bindNewDeviceSuccess
        )

        // 原本在這裡建立 Notification Channel 移至MyApplication

        // 檢查我們的權限是不是都拿到了 delay的原因我推測是因為可能會去到其他頁面 導致這頁被關閉會出錯 現在改為權限分開請求
//        Handler().postDelayed({
//            checkPermissions()
//        }, 100)

        // FCM Firebase Token
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrEmpty()) {
                        SecuredPreferenceStoreManager.simpleEditAndApply(
                            Config.SP_FCM_TOKEN,
                            task.result
                        )
                    } else {
                        Timber.d("Fetching FCM registration token failed")
                        Timber.d(task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 初始化參數 這裡不再需要
//        pid = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_PID, "")
//        ticket = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_TICKET, "")

        // Fid Firebase Installations Unique Id
        try {
            FirebaseInstallations.getInstance().id
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrEmpty()) {
//                        SecuredPreferenceStoreManager.simpleEditAndApply(
//                            Config.PREF_LOGIN_FID,
//                            task.result
//                        )
                        SecuredPreferenceStoreManager.simpleEditAndApply(
                            Config.SP_FIREBASE_INSTALLATIONS_UNIQUE_ID,
                            task.result
                        )
                    } else {
                        Timber.d("Fetching FirebaseInstallations token failed")
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // CelebrusCSA 初始化
        CelebrusCSAUtil.start(this)
        viewBinding.webView.setUp(this, this, this) { filePathCallback, fileChooserParams ->
            this@MainActivity.filePathCallback?.onReceiveValue(null)
            this@MainActivity.filePathCallback = filePathCallback

            val intent = fileChooserParams.createIntent()
            fileChooserLauncher.launch(intent)
        }

        CelebrusCSAUtil.sessionSharing(this)

        // 如果以前分享的圖片還在 刪除
        ShareUtil.delShareImage(this)

        // 以前會在這裡初始化 GooglePlayCore類別 已經直接取代掉 詳見scoreGooglePlay
        // 檢查Google Play
        ProviderInstaller.installIfNeededAsync(this, this)

        observeApiResultSetUp()

        // 測試用
        forTest()

        val isLogin = SecuredPreferenceStoreManager.getBoolean(Config.SP_LOGIN_AUTH, false)
        if (isLogin) {
            // 取得使用者資料
            mainViewModel.getLanding()
        }
    }


    private fun addNotification() {
        val random = Random.Default.nextInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = permissionManager.hasPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
            if (perm) {
                myNotificationManager.addNotification("測試id :$random", random)
            } else {
                permissionManager.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) { granted, denied ->
                    if (granted) {
                        myNotificationManager.addNotification("測試id :$random", random)
                    }
                }
            }
        } else {
            myNotificationManager.addNotification("測試id :$random", random)
        }
    }

    private fun updateBadge(badgeCount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = permissionManager.hasPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
            if (perm) {
                myNotificationManager.updateBadge(badgeCount)
            } else {
                permissionManager.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) { granted, denied ->
                    if (granted) {
                        myNotificationManager.updateBadge(badgeCount)
                    }
                }
            }
        } else {
            myNotificationManager.updateBadge(badgeCount)
        }
        this.badgeCount = badgeCount
        viewBinding.updateBadge.text =
            "${getString(R.string.main_simulate_update_badge)}(${badgeCount})"
    }

    private fun forTest() {
        viewBinding.testCsso.setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java))
        }
        viewBinding.dummyData.setOnClickListener {
            if (healthConnectViewModel == null) {
                toast(R.string.main_health_connect_not_available)
                return@setOnClickListener
            }
            healthConnectViewModel?.let { healthConnectViewModel ->
                healthConnectViewModel.writeAndCleanDummyHealthDataForPast30Days {
                    debugToast(R.string.main_health_connect_dummy_insert_finished)
                }
            }
        }

        viewBinding.logout.setOnClickListener {
            toLogin()
            return@setOnClickListener
        }
        viewBinding.clearBadge.setOnClickListener {
            updateBadge(0)
        }

        viewBinding.addNotification.setOnClickListener {
            addNotification()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewBinding.webView.backIfValid()) {
                    finish()
                }
            }
        })
    }

    private fun checkPermissions() {
        val permissionList = mutableListOf(
            Manifest.permission.CAMERA,
//            Manifest.permission.INTERNET,
//            Manifest.permission.ACCESS_NETWORK_STATE,
            // 上面兩個不需要運行時請求
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionList.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        val requestPermissionFunction = {
            permissionManager.requestPermissions(permissionList.toTypedArray()) { granted, denied ->
                if (granted) {
                    Timber.d("所有權限都已獲得")
                } else {
                    denied.forEach {
                        val deniedText = permissionManager.getPermissionDeniedText(it, this)
                        if (!deniedText.isNullOrEmpty()) {
                            toast(deniedText)
                        }
                    }
                }
            }
        }
        if (!permissionManager.hasPermissions(this, permissionList.toTypedArray())) {
            val atLeastOneRationale =
                permissionManager.shouldShowRationale(permissionList.toTypedArray()) { permission ->
                    val rationale = permissionManager.getPermissionRationaleText(permission, this)
                    if (!rationale.isNullOrEmpty()) {
                        toast(rationale)
                    }
                }
            if (atLeastOneRationale) {
                getAlertDialog(
                    this,
                    getString(R.string.main_permission_rationale),
                    false,
                    true,
                    getString(R.string.confirm1),
                    {
                        requestPermissionFunction()
                    })
            } else {
                requestPermissionFunction()
            }
        }
    }

    private fun observeApiResultSetUp() {
        // 使用者資訊
        observeOnLifeCycle(mainViewModel.userInfoFlow) { userInfoResponse ->
            // 取得使用者資訊 將現在綁定的設備儲存
            var deviceType = NONE
            if (userInfoResponse.bindingAndroid != null && userInfoResponse.bindingAndroid) {
                // 綁定 Health Connect
                deviceType = HEALTH_CONNECT
            } else if (userInfoResponse.bindingFibit != null && userInfoResponse.bindingFibit) {
                // 綁定FITBIT
                deviceType = FITBIT
            } else if (userInfoResponse.bindingGarmin != null && userInfoResponse.bindingGarmin) {
                // 綁定Garmin
                deviceType = GARMIN
            }

            SecuredPreferenceStoreManager.simpleEditAndApply(
                Config.SP_BIND_CURRENT_DEVICE,
                deviceType.value
            )

            SecuredPreferenceStoreManager.simpleEditAndApply(
                Config.SP_USER_INFO,
                getGson().toJson(userInfoResponse)
            )

//            if (userInfoResponse.completeOnboarding != null && !userInfoResponse.completeOnboarding) {
//                toOnBoarding(userInfoResponse)
//            }
        }
        observeOnLifeCycle(mainViewModel.landingFlow) { landingResponse ->
            // 需要Onboard
            if (landingResponse.completeOnboarding != null && !landingResponse.completeOnboarding) {
                toOnBoarding(
                    landingResponse.getAvailableNickName(),
                    landingResponse.referrerCode ?: ""
                )
            } else {
                // 應該不需要呼叫了?
//                mainViewModel.getUserInfo()
            }


            viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
        }
    }

    override fun onReceivedEvent(eventName: String?, result: String) {
//        super.onReceivedEvent(eventName, result)
        if (eventName == Config.EVENT_NO_TOKEN_TO_LOGIN) {
            toLogin()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val uri = intent.data
        if (uri == null) return
        if (TextUtils.isEmpty(uri.host)) return
        val hostTypes = HostTypes.getFromValue(uri.host!!)
        if (hostTypes == null) return

        when (hostTypes) {
            HOME -> {
//                viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
            }

            LOGIN -> {
                toLogin()
            }

            LOGIN_SUCCESS -> {
                // 新版照理來說不會透過url告知登入成功 有API了 預防萬一留著
//                loginSuccess(uri)
            }

            LOGIN_FAILURE -> {
                loginFailure()
            }

            USER_INFO -> {
                // 新版會透過API取得使用者資料 照理來說不會靠url
//                val pwPageFlag =
//                    SecuredPreferenceStoreManager.getBoolean(Config.PW_PAGE_FLAG, false)
//                if (pwPageFlag) {
//                    viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
//                } else {
//                    viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl + "my/preferences")
//                }
            }

            ONBOARDING -> {
                // 新版不會透過webview走到Onboard 如果真的走到了 取得使用者資訊 看是不是真的有需要走初次流程
                mainViewModel.getLanding()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        // 最新的鼓勵使用Launcher 會到這裡表示不得已 否則請勿使用
        if (resultCode == RESULT_OK && requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            googleAuthCode = null
            try {
                val account = task.getResult<ApiException?>(ApiException::class.java)
                googleAuthCode = account.serverAuthCode
            } catch (e: ApiException) {
                toast(R.string.onboard_connect_fail)
                e.printStackTrace()
            }
            // 流程結束 看看最後的authCode狀況
            gsoAuthCodeProcessFinish()
        }
        if (requestCode == GOOGLE_ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which causes the fragment to delay until
            // onPostResume.
            retryProviderInstall = true
        }
    }

    private fun sameDeviceCallback(deviceType: DeviceType) {
//        toast(
//            String.format(
//                Locale.getDefault(),
//                getString(R.string.main_binding_same_device),
//                deviceType.displayName
//            )
//        )

        if (deviceType == HEALTH_CONNECT) {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                viewBinding.dummyData.visibility = View.VISIBLE
            }
        }

        // 如果有需要透過JS回傳綁定結果
        postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, true.enableToString().quoteJS())

        if (deviceType != NONE) {
            CommonDialog(this).apply {
                oneButtonInit(
                    getString(R.string.binding_success_title), getString(R.string.binding_success_body), R.drawable.alert_1,
                    showButtons = true,
                    canceledOnTouchOutside = true,
                    text = getString(R.string.ok)
                )
            }.show()
        }
    }


    private fun bindingRemoved(deviceType: DeviceType) {
        debugToast("${deviceType.displayName} removed")
    }

    private fun bindNewDeviceSuccess(deviceType: DeviceType, data: String?) {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            viewBinding.dummyData.visibility = View.GONE
        }
        when (deviceType) {
            HEALTH_CONNECT -> {
                debugToast(R.string.onboard_connect_success)
                lifecycleScope.launch(Dispatchers.Main.immediate) {
                    viewBinding.dummyData.visibility = View.VISIBLE
                }
                // 如果有需要透過JS回傳綁定結果
                postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, true.enableToString().quoteJS())
            }

            GARMIN -> {
                val garminData = getGson().fromJson(data, GarminData::class.java)

                if (garminData.oauthToken != null && garminData.oauthTokenSecret != null) {
                    debugToast("t = ${garminData.oauthToken}\ns = ${garminData.oauthTokenSecret}")

                    // 如果有需要透過JS回傳綁定結果
                    val garminModel =
                        GarminModel(garminData.oauthToken, garminData.oauthTokenSecret)
                    postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, getGson().toJson(garminModel))
                }
            }

            FITBIT -> {
                val fitbitData = getGson().fromJson(data, FitbitData::class.java)

                if (fitbitData.accessToken != null && fitbitData.refreshToken != null) {
                    debugToast("t = ${fitbitData.accessToken}\nr = ${fitbitData.refreshToken}")

                    // 如果有需要透過JS回傳綁定結果
                    val fitbitModel = FitbitModel(fitbitData.accessToken, fitbitData.refreshToken)
                    postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, getGson().toJson(fitbitModel))
                }
            }

            NONE -> {}
        }

        if (deviceType != NONE) {
            CommonDialog(this).apply {
                oneButtonInit(
                    getString(R.string.binding_success_title), getString(R.string.binding_success_body), R.drawable.alert_1,
                    showButtons = true,
                    canceledOnTouchOutside = true,
                    text = getString(R.string.ok)
                )
            }.show()
        }
    }

    private fun gsoAuthCodeProcessFinish() {
        if (TextUtils.isEmpty(googleAuthCode)) {
            toast(R.string.onboard_connect_fail)
            Timber.d("Fail to connect google fit, no auth code")
        } else {
            viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl + "health/connect?device=google&a=" + googleAuthCode)
            toast(R.string.onboard_connect_success)
        }
    }

//    private fun loginSuccess(uri: Uri) {
//        Timber.d("CSSO Login Success: ${Utils.formatDate()}")
//        val ticket = uri.getQueryParameter(QUERY_PARAM_TICKET)
//
//        if (TextUtils.isEmpty(ticket)) {
//            Toast.makeText(this, getString(R.string.login_no_ticket), Toast.LENGTH_SHORT).show()
//            toLogin()
//        } else {
//            val pid = intent.getStringExtra(KEY_PID)
//            SecuredPreferenceStoreManager.editAndApply { prefEditor ->
//                prefEditor.putBoolean(Config.PREF_LOGIN_AUTH, true)
//                if (!TextUtils.isEmpty(pid)) {
//                    prefEditor.putString(Config.PREF_LOGIN_USERNAME, pid)
//                }
//                prefEditor.putString(Config.PREF_LOGIN_TICKET, ticket)
//            }
//            val pwPageFlag = SecuredPreferenceStoreManager.getBoolean(Config.PW_PAGE_FLAG, false)
//
//            if (pwPageFlag) {
//                viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().tcavUrl + "other/user/teamwalk")
//            } else {
//                viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
//            }
//        }
//    }

    private fun loginFailure() {
        Timber.d("Login Failed - ${Utils.formatDate()}")

        toLogin()
    }

    private fun toOnBoarding(nickName: String, referrerCode: String) {
        val onboardingIntent = PromoteActivity.startPromoteActivity(this, nickName, referrerCode)
        startActivity(onboardingIntent)
    }

    private fun securityCheck() {
        val knowsRoot = SecuredPreferenceStoreManager.getBoolean(Config.SP_KNOWS_ROOT, false)
        if (DeviceUtil.isDeviceRooted() && !knowsRoot) {
            getAlertDialog(
                context = this,
                message = getString(R.string.alert_root_message),
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {
                    doBusiness()
                    SecuredPreferenceStoreManager.editAndApply { editor ->
                        editor.putBoolean(Config.SP_KNOWS_ROOT, true)
                    }
                }
            )
        } else if (!DeviceUtil.isDeviceSecure(this) && !isKnowsDeviceSecure) {
            getAlertDialog(
                context = this,
                message = getString(R.string.alert_no_password_message),
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {
                    doBusiness()
                    isKnowsDeviceSecure = true
                }
            )
        } else if (DeviceUtil.isReverseToolRunning(this) && !isKnowsReverseToolRunning) {
            getAlertDialog(
                context = this,
                message = getString(R.string.alert_reverse_tool_message),
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {
                    doBusiness()
                    isKnowsReverseToolRunning = true
                }
            )
        } else if (DeviceUtil.isCovered(this) && !isKnowsCovered) {
            // 舊版沒加 !isKnowsCovered 應該是忘記了 加上
            getAlertDialog(
                context = this,
                message = getString(R.string.alert_covered_message),
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {
                    doBusiness()
                    isKnowsCovered = true
                }
            )
        } else {
            doBusiness()
        }
    }

    private fun doBusiness() {
        // 這邊檢查是否登入
        val isLogin = SecuredPreferenceStoreManager.getBoolean(Config.SP_LOGIN_AUTH, false)
        if (!isLogin) {
            toLogin()
            viewBinding.logout.text = getString(R.string.main_not_logged_in)
        } else {
            viewBinding.logout.text = getString(R.string.main_simulate_logout)

//            // 舊有邏輯
//            val uri = intent.data
//            if (uri != null) {
//                intent.data = null
//            } else {
//                val url = viewBinding.webView.url
//                if (url != null) {
//                    if (url.endsWith(URL_END_LOGOUT)) {
//                        toLogin()
//                    }
//                    if (url.endsWith(URL_END_BLANK)) {
//                        Timber.d("Logout by blank")
//                        toLogin()
//                    }
//                    if (intent.action != null && intent.action.equals(ON_BOARD_FINISH)) {
//                        viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl + "main")
//                    }
//
//                    if (url.endsWith(URL_END_MY_PREFERENCES)) {
//                        viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl + "my/preferences")
//                    }
//                    Timber.d("Current url - $url")
//                } else {
//                    checkAuthenticated()
//                }
//            }

            if (!isOnline()) {
                toast(R.string.main_is_not_online)
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun clearSensitiveData(isDestroy: Boolean) {
        if (isDestroy) {
            viewBinding.webView.loadUrl("about:blank")
            SensitiveDataUtil.clearWebViewSensitiveData(this, viewBinding.webView, isDestroy)
        }
    }

    private fun toLogin() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        SecuredPreferenceStoreManager.editAndApply { prefEditor ->
//            prefEditor.putBoolean(Config.PREF_LOGIN_AUTH, false)
            prefEditor.putBoolean(Config.SP_LOGIN_AUTH, false)
//            prefEditor.putString(Config.PREF_LOGIN_TICKET, "")
//            prefEditor.putString(Config.PREF_LOGIN_USERNAME, "")
            prefEditor.putString(Config.SP_LOGIN_JWT_TOKEN, "")
        }

        viewBinding.webView.post {
            viewBinding.webView.loadUrl("about:blank")
        }

        val loginIntent = Intent(this, LoginActivity::class.java)
        loginLauncher.launch(loginIntent)
    }

    private fun onProviderInstallerNotAvailable() {
        // This is reached if the provider can't be updated for some reason.
        // App should consider all HTTP communication to be vulnerable and take
        // appropriate action.
        toast(R.string.main_provider_installer_error)
    }

    override fun onResume() {
        super.onResume()

        securityCheck()

        viewBinding.webView.post {
            viewBinding.webView.evaluateJavascript(
                "window.WebAppBridge.resumeAPP()",
                null
            )

            context.debugToast("resumeAPP()")
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (retryProviderInstall) {
            // It's safe to retry installation.
            ProviderInstaller.installIfNeededAsync(this, this)
        }
        retryProviderInstall = false
    }

    override fun onStop() {
        super.onStop()
        if (clearCache != null && clearCache!!) {
            viewBinding.webView.clearCache(true)
        }
        clearSensitiveData(false)
    }

    override fun onDestroy() {
        clearSensitiveData(true)
        super.onDestroy()
    }

    override fun setGraphicalLogin(enable: String) {
        val patternIntent = Intent(this, PatternSetupActivity::class.java)
        patternLauncher.launch(patternIntent)
    }

    override fun bindingGoogleHealth(enable: String) {
        bindingManager.bindNewDevice(HEALTH_CONNECT)
    }

    fun getHealthConnectData() {
        if (healthConnectViewModel == null) {
            toast(R.string.main_health_connect_not_available)

            // 如果有需要透過JS回傳推播設定結果
            postEvent(
                EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                getGson().toJson(SyncHealthDataModel(emptyList(), emptyList()))
            )

            return
        }
        healthConnectViewModel?.let { healthConnectViewModel ->
            healthConnectViewModel.getAllData { sleepData, stepsData ->
                val syncHealthDataModel = SyncHealthDataModel(stepsData, sleepData)

//                val json = Gson().toJson(syncHealthDataModel)
//                Timber.d(json)

                // 如果有需要透過JS回傳推播設定結果
                postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, getGson().toJson(syncHealthDataModel))
            }
        }
    }

    override fun bindingGarminHealth(enable: String) {
        bindingManager.bindNewDevice(GARMIN)
    }

    override fun bindingFitbitHealth(enable: String) {
        bindingManager.bindNewDevice(FITBIT)

//        // 版本29之後才需要要求此權限 29之前的可以直接執行
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val permissions = ArrayList<String>()
//            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
//            getPermissionAndCallback(
//                permissions,
//                MainWebViewJava.SimpleCallback {
//                    accessGoogleFit()
//                })
//        } else {
//            accessGoogleFit()
//        }
    }

    override fun setPushMessageStatus(enable: String) {
        val permissionList = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val permissionGratedCallback = {
            SecuredPreferenceStoreManager.editAndApply {
                it.putBoolean(Config.SP_NOTIFICATION, enable.enableToBoolean())
            }
            toast(R.string.main_push_notification_setting_done)

            // 如果有需要透過JS回傳推播設定結果
            postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, true.enableToString().quoteJS())
        }

        val requestPermissionFunction = {
            permissionManager.requestPermissions(permissionList.toTypedArray()) { granted, denied ->
                if (granted) {
                    permissionGratedCallback()
                } else {
                    denied.forEach {
                        val deniedText = permissionManager.getPermissionDeniedText(it, this)
                        if (!deniedText.isNullOrEmpty()) {
                            toast(deniedText)
                        }
                    }
                    // 如果有需要透過JS回傳推播設定結果
                    postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, false.enableToString().quoteJS())
                }
            }
        }
        if (!permissionManager.hasPermissions(this, permissionList.toTypedArray())) {
            val atLeastOneRationale =
                permissionManager.shouldShowRationale(permissionList.toTypedArray()) { permission ->
                    val rationale = permissionManager.getPermissionRationaleText(permission, this)
                    if (!rationale.isNullOrEmpty()) {
                        toast(rationale)
                    }
                }
            if (atLeastOneRationale) {
                getAlertDialog(
                    this,
                    getString(R.string.notification_permission_rationale),
                    false,
                    true,
                    getString(R.string.confirm1), {
                        requestPermissionFunction()
                    }
                )
            } else {
                requestPermissionFunction()
            }
        } else {
            permissionGratedCallback()
        }
    }

    override fun syncHealthData() {
        getHealthConnectData()
    }

    override fun updatePushCount(notifyCount: Int) {
        myNotificationManager.updateBadge(notifyCount)
    }

    override fun logout() {
        toLogin()
    }

    override fun saveDataToFile(data: String, fileName: String) {
        tempFileData = data
        tempFileName = fileName

        createDocumentLauncher.launch(fileName)
    }


    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
        GoogleApiAvailability.getInstance().apply {
            if (isUserResolvableError(errorCode)) {
                // Recoverable error. Show a dialog prompting the user to
                // install/update/enable Google Play services.
                showErrorDialogFragment(
                    this@MainActivity,
                    errorCode,
                    GOOGLE_ERROR_DIALOG_REQUEST_CODE
                ) {
                    // The user chose not to take the recovery action.
                    onProviderInstallerNotAvailable()
                }
            } else {
                onProviderInstallerNotAvailable()
            }
        }
    }

    override fun onProviderInstalled() {
        Timber.d("ProviderInstalled")
    }

    override fun onWebviewPageStarted() {
        onLoading(true)
    }

    override fun onWebviewPageFinished() {
        onLoading(false)
    }
}