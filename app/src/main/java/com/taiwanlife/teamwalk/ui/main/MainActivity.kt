package com.taiwanlife.teamwalk.ui.main

import android.Manifest
import android.R.attr.type
import android.app.Activity
import android.app.ComponentCaller
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_MSG
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TITLE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TYPE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_URL
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityMainBinding
import com.taiwanlife.teamwalk.java_utils.CelebrusCSAUtil
import com.taiwanlife.teamwalk.java_utils.DeviceUtil
import com.taiwanlife.teamwalk.remote.HealthConnectRepository
import com.taiwanlife.teamwalk.service.TWFirebaseMessagingService
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
import com.taiwanlife.teamwalk.ui.main.webview.MyWebMessageListener
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
import com.taiwanlife.teamwalk.utils.SecurityCheckManager
import com.taiwanlife.teamwalk.utils.ShareUtil
import com.taiwanlife.teamwalk.utils.Utils
import com.taiwanlife.teamwalk.utils.Utils.stringToNotificationType
import com.taiwanlife.teamwalk.utils.debugToast
import com.taiwanlife.teamwalk.utils.enableToBoolean
import com.taiwanlife.teamwalk.utils.enableToString
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.quoteJS
import com.taiwanlife.teamwalk.utils.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Locale
import kotlin.random.Random

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }),
    ProviderInstaller.ProviderInstallListener,
    MyWebMessageListener.AsyncCallbacks, MyWebView.WebviewLoadingCallback {

    companion object {
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 4
        const val GOOGLE_ERROR_DIALOG_REQUEST_CODE = 4

        private const val QUERY_PARAM_TICKET = "ticket"
        private const val KEY_PID = "pid"
    }

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

            if (result.resultCode == RESULT_OK) {
                val isGraphicalLoginSet = result.data?.getBooleanExtra(
                    PatternSetupActivity.KEY_IS_GRAPHICAL_LOGIN_SET,
                    false
                ) ?: false

                postEvent(
                    EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                    isGraphicalLoginSet.enableToString().quoteJS()
                )
            }
        }


    private var tempFileData: String? = null
    private var tempFileName: String? = null
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            if (tempFileData == null || tempFileName == null) return@registerForActivityResult

            mainViewModel.startToWriteFile(contentResolver, uri, tempFileData!!) {
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
            ::bindingRemoved,
            ::bindNewDeviceSuccess
        )

        registerNetworkCallback({}) {
            showNoInternetAlert()
        }
        // 原本在這裡建立 Notification Channel 移至MyApplication

        // 檢查我們的權限是不是都拿到了 delay的原因我推測是因為可能會去到其他頁面 導致這頁被關閉會出錯 現在改為權限分開請求
//        Handler().postDelayed({
//            checkPermissions()
//        }, 100)

        val options = FirebaseApp.getInstance().options
        debugToast("ProjectID: ${options.projectId}")

        // FCM Firebase Token
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrEmpty()) {
                        SecuredPreferenceStoreManager.simpleEditAndApply(
                            Config.SP_FCM_IDENTIFIER,
                            task.result
                        )
                        debugToast("取得FCM Token ${task.result}")
                    } else {
                        Timber.d("Fetching FCM registration token failed")
                        Timber.d(task.exception?.message)
                        val oldFCMToken =
                            SecuredPreferenceStoreManager.getString(Config.SP_FCM_IDENTIFIER, "")
                        if (oldFCMToken.isNotEmpty()) {
                            debugToast("取得新的FCMToken失敗, 但有舊的")
                        } else {
                            debugToast("取得新的FCMToken失敗, 且沒有舊的")
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            debugToast("取得FCM Token 失敗 catch => ${e.message}")
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
        viewBinding.webView.setUp(this, this, this, {
            finishAffinity()
        }) { filePathCallback, fileChooserParams ->
            this@MainActivity.filePathCallback?.onReceiveValue(null)
            this@MainActivity.filePathCallback = filePathCallback

            val intent = fileChooserParams.createIntent()
            fileChooserLauncher.launch(intent)
        }

        CelebrusCSAUtil.sessionSharing(this)

        // 如果以前分享的圖片還在 刪除
        ShareUtil.delShareImage(this)

        // 重置OnBoarding Flag
        SecuredPreferenceStoreManager.editAndApply {
            it.putBoolean(Config.SP_BINDING_FROM_ONBOARD, false)
        }

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
        } else {
            toLogin()
        }
        // 檢查版本 改為登入頁檢查 如果webview存在時會由webview做檢查並踢到登入頁
//            mainViewModel.getSysParam()
    }


    private fun addNotification() {
        val random = Random.Default.nextInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = permissionManager.hasPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
            if (perm) {
                myNotificationManager.testFcmNotification()
//                myNotificationManager.addNotification("測試id :$random", random)
            } else {
                permissionManager.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) { granted, denied ->
                    if (granted) {
                        myNotificationManager.testFcmNotification()
//                        myNotificationManager.addNotification("測試id :$random", random)
                    }
                }
            }
        } else {
            myNotificationManager.testFcmNotification()
//            myNotificationManager.addNotification("測試id :$random", random)
        }
    }

    private fun updateBadge(badgeCount: Int) {
        val pendingIntent = MyNotificationManager.getPendingIntentForBadge(this, 1002, badgeCount)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = permissionManager.hasPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
            if (perm) {
                myNotificationManager.updateBadge(pendingIntent, badgeCount)
            } else {
                permissionManager.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) { granted, denied ->
                    if (granted) {
                        myNotificationManager.updateBadge(pendingIntent, badgeCount)
                    }
                }
            }
        } else {
            myNotificationManager.updateBadge(pendingIntent, badgeCount)
        }
        viewBinding.updateBadge.text =
            "${getString(R.string.main_simulate_update_badge)}(${badgeCount})"
    }

    private fun forTest() {
        viewBinding.testFitbit.setOnClickListener {
            bindingManager.bindNewDevice(DeviceType.FITBIT)
        }
        viewBinding.testGarmin.setOnClickListener {
            bindingManager.bindNewDevice(DeviceType.GARMIN)
        }
        viewBinding.testHc.setOnClickListener {
            bindingManager.bindNewDevice(DeviceType.HEALTH_CONNECT)
        }
        viewBinding.test.setOnClickListener {
//            healthConnectViewModel?.i = 0
//            healthConnectViewModel?.getAllData{ _, _ -> }
        }
        viewBinding.patternLcokTest.setOnClickListener {
            val patternIntent = Intent(this, PatternSetupActivity::class.java)
            patternIntent.putExtra(PatternSetupActivity.KEY_ATTEMPT_ENABLE, "Y")
            patternLauncher.launch(patternIntent)
        }
//        viewBinding.dummyData.setOnClickListener {
//            if (healthConnectViewModel == null) {
//                toast(R.string.main_health_connect_not_available)
//                return@setOnClickListener
//            }
//            healthConnectViewModel?.let { healthConnectViewModel ->
//                healthConnectViewModel.writeAndCleanDummyHealthData {
//                    debugToast("假資料插入完成")
//                }
//            }
//        }
        viewBinding.deleteData.setOnClickListener {
            if (healthConnectViewModel == null) {
                toast(R.string.main_health_connect_not_available)
                return@setOnClickListener
            }
            healthConnectViewModel?.let { healthConnectViewModel ->
                healthConnectViewModel.deleteAllOurData {
                    toast("刪除完成")
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
            if (!landingResponse.accessToken.isNullOrEmpty() && !landingResponse.refreshToken.isNullOrEmpty() && !landingResponse.jti.isNullOrEmpty()) {
                val garminData = GarminData(
                    "",
                    "",
                    "",
                    landingResponse.accessToken,
                    landingResponse.refreshToken,
                    landingResponse.jti
                )

                SecuredPreferenceStoreManager.simpleEditAndApply(
                    Config.SP_BIND_GARMIN,
                    getGson().toJson(garminData)
                )
            }

            viewBinding.webView.loadUrl(getEnvironmentConfig().webUrl)

            handleRedirectIntent(intent)
        }
    }

    override fun onReceivedEvent(eventName: String?, result: String) {
//        super.onReceivedEvent(eventName, result)
        if (eventName == Config.EVENT_NO_ID_TO_LOGIN) {
            toLogin()
        } else if(eventName == Config.EVENT_ONBOARDING_CONNECT_COMPLETE_REFRESH_HOME) {
            viewBinding.webView.reload()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (isLogin()) {
            handleRedirectIntent(intent)
        }


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


    private fun bindingRemoved(deviceType: DeviceType) {
        SecuredPreferenceStoreManager.editAndApply {
            it.putString(Config.SP_BIND_FITBIT, "")
            it.putString(Config.SP_BIND_GARMIN, "")
        }
        // 如果有需要透過JS回傳綁定結果
        postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, false.enableToString().quoteJS())
    }

    private fun bindNewDeviceSuccess(deviceType: DeviceType, data: String?) {
        if (SecuredPreferenceStoreManager.getBoolean(Config.SP_BINDING_FROM_ONBOARD, false)) {
            // 來自OnBoarding 交由那邊處理
            return
        }
//        lifecycleScope.launch(Dispatchers.Main.immediate) {
//            viewBinding.dummyData.visibility = View.GONE
//        }
        when (deviceType) {
            HEALTH_CONNECT -> {
//                lifecycleScope.launch(Dispatchers.Main.immediate) {
//                    viewBinding.dummyData.visibility = View.VISIBLE
//                }
                // 如果有需要透過JS回傳綁定結果
                postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, true.enableToString().quoteJS())
            }

            GARMIN -> {
                val garminData = getGson().fromJson(data, GarminData::class.java)

//                if (garminData.oauthToken != null && garminData.oauthTokenSecret != null) {
                if (garminData.accessToken.isNotEmpty() && garminData.refreshToken.isNotEmpty() && garminData.jti.isNotEmpty()) {
                    // 如果有需要透過JS回傳綁定結果
                    val garminModel =
                        GarminModel(
                            garminData.oauthToken,
                            garminData.oauthTokenSecret,
                            garminData.accessToken,
                            garminData.refreshToken,
                            garminData.jti
                        )
                    postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, getGson().toJson(garminModel))
                }
            }

            FITBIT -> {
                val fitbitData = getGson().fromJson(data, FitbitData::class.java)

                if (fitbitData.accessToken != null && fitbitData.refreshToken != null) {
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
                    getString(R.string.binding_success_title),
                    getString(R.string.binding_success_body),
                    R.drawable.alert_1,
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
            viewBinding.webView.loadUrl(getEnvironmentConfig().webUrl + "health/connect?device=google&a=" + googleAuthCode)
            toast(R.string.onboard_connect_success)
        }
    }

    private fun loginFailure() {
        Timber.d("Login Failed - ${Utils.formatDate()}")

        toLogin()
    }

    private fun toOnBoarding(nickName: String, referrerCode: String) {
        val onboardingIntent = PromoteActivity.startPromoteActivity(this, nickName, referrerCode)
        startActivity(onboardingIntent)
    }

    private fun securityCheck() {
        val result = SecurityCheckManager.runAll(this)
        if (!result.passed) {
            getAlertDialog(
                context = this,
                message = result.errorMessage ?: "",
                icon = R.mipmap.ic_launcher,
                isCancelable = false,
                shouldShow = true,
                positiveText = getString(R.string.understand_and_continue),
                positiveOnClick = {
                    doBusiness()
                }
            )
        } else {
            doBusiness()
        }
    }

    private fun doBusiness() {
        // 這邊檢查是否登入
        if (!isLogin()) {
            toLogin()
            viewBinding.logout.text = getString(R.string.main_not_logged_in)
        } else {
            viewBinding.logout.text = getString(R.string.main_simulate_logout)

            if (!isOnline()) {
                showNoInternetAlert()
            }
        }
    }

    private fun isLogin(): Boolean {
        return SecuredPreferenceStoreManager.getBoolean(Config.SP_LOGIN_AUTH, false)
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
            clearSensitiveData(viewBinding.webView)
//            SensitiveDataUtil.clearWebViewSensitiveData(this, viewBinding.webView, isDestroy)
        }
    }

    private fun clearLoginData() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        SecuredPreferenceStoreManager.editAndApply { prefEditor ->
//            prefEditor.putBoolean(Config.PREF_LOGIN_AUTH, false)
            prefEditor.putBoolean(Config.SP_LOGIN_AUTH, false)
//            prefEditor.putString(Config.PREF_LOGIN_TICKET, "")
//            prefEditor.putString(Config.PREF_LOGIN_USERNAME, "")
            prefEditor.putString(Config.SP_LOGIN_JWT, "")
            prefEditor.putString(Config.SP_CASTGC, "")
            prefEditor.putString(Config.SP_BIND_GARMIN, "")
            prefEditor.putString(Config.SP_BIND_FITBIT, "")
            prefEditor.putString(Config.SP_BIND_CURRENT_DEVICE, "")
            prefEditor.putBoolean(Config.SP_BINDING_FROM_ONBOARD, false)
        }

        viewBinding.webView.post {
            viewBinding.webView.loadUrl("about:blank")
        }
    }

    private fun toLogin() {
        clearLoginData()

        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
        if (enable == "N") {
            postEvent(
                EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                false.enableToString().quoteJS()
            )
        } else {
            val patternIntent = Intent(this, PatternSetupActivity::class.java)
            patternIntent.putExtra(PatternSetupActivity.KEY_ATTEMPT_ENABLE, enable)
            patternLauncher.launch(patternIntent)
        }
    }

    override fun bindingGoogleHealth(enable: String) {
        if (enable.enableToBoolean()) {
            bindingManager.bindNewDevice(HEALTH_CONNECT)
        } else {
            bindingManager.removeDevice(HEALTH_CONNECT)
        }
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
        if (enable.enableToBoolean()) {
            bindingManager.bindNewDevice(GARMIN)
        } else {
            bindingManager.removeDevice(GARMIN)
        }
    }

    override fun bindingFitbitHealth(enable: String) {
        if (enable.enableToBoolean()) {
            bindingManager.bindNewDevice(FITBIT)
        } else {
            bindingManager.removeDevice(FITBIT)
        }
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
        myNotificationManager.updateBadge(MyNotificationManager.getPendingIntentForBadge(this, 1003, notifyCount),notifyCount)
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

    private fun handleRedirectIntent(intent: Intent) {
        val type = intent.getStringExtra(NOTIFICATION_KEY_TYPE)
        val url = intent.getStringExtra(NOTIFICATION_KEY_URL)
        if (!type.isNullOrEmpty() && !url.isNullOrEmpty()) {
            val notificationType = stringToNotificationType(type)

            when (notificationType) {
                Config.NotificationType.NONE -> {}
                Config.NotificationType.URL -> {
                    CommonDialog(this).apply {
                        twoButtonInit(
                            getString(R.string.notification_url_alert_title),
                            getString(R.string.notification_url_alert_message),
                            R.drawable.alert_1,
                            showButtons = true,
                            canceledOnTouchOutside = false,
                            positiveText = getString(R.string.confirm2),
                            positiveOnClick = {
                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                startActivity(intent)
                            },
                            negativeText = getString(R.string.cancel),
                            negativeOnClick = {

                            }
                        )
                    }.show()
                }

                Config.NotificationType.APP_PAGE -> {
                    val notificationAppPageData =
                        Config.listOfNotificationAppPageData.find { it.urlValue == url }
                            ?: Config.NotificationAppPageData("", "")
                    val uri = getEnvironmentConfig().webUrlBase.toUri()
                        .buildUpon()
                        .appendEncodedPath(notificationAppPageData.realPath)
                        .build()
                        .toString()
                    viewBinding.webView.loadUrl(uri)
                }
            }
            intent.putExtra(NOTIFICATION_KEY_TYPE, "")
            intent.putExtra(NOTIFICATION_KEY_URL, "")
            setIntent(intent)
            return
        }

//
//        if (intent.data != null) {
//            val uri = intent.data!!
//            when (uri.host) {
//                "webconnect" -> {
//                    forwardIntent(intent, ConnectFitbitSuccessActivity::class.java)
//                    return
//                }
//
//                "webconnectgarmin" -> {
//                    forwardIntent(intent, ConnectGarminSuccessActivity::class.java)
//                    return
//                }
//            }
//        }
    }

//    private fun forwardIntent(
//        original: Intent,
//        target: Class<out BaseActivity<*>>
//    ) {
//        val newIntent = Intent(original).apply {
//            setClass(this@MainActivity, target)
//
//            // 避免返回 PortalActivity
////            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        }
//
//        startActivity(newIntent)
//    }

    fun registerNetworkCallback(
        onAvailable: () -> Unit,
        onLost: () -> Unit
    ) {
        val cm =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    onAvailable()
                }

                override fun onLost(network: Network) {
                    onLost()
                }
            }
        )
    }

    fun showNoInternetAlert() {
        CommonDialog(this).apply {
            oneButtonInit(
                "", getString(R.string.main_is_not_online), R.drawable.alert_1,
                showButtons = true,
                canceledOnTouchOutside = false,
                text = getString(R.string.ok),
                onClick = {
                    when (BuildConfig.BUILD_TYPE) {
                        "uat" -> {}

                        else -> {
                            finishAffinity()
                        }
                    }
                }
            )
        }.show()
    }
}