package com.taiwanlife.teamwalk.ui.main

import android.Manifest
import android.app.Activity
import android.app.ComponentCaller
import android.content.ActivityNotFoundException
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
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.Config.EVENT_EXECUTE_JAVASCRIPT_CALLBACK
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_TYPE
import com.taiwanlife.teamwalk.Config.NOTIFICATION_KEY_URL
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.EnvironmentManager.getEnvironmentConfig
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.base.BaseActivity
import com.taiwanlife.teamwalk.databinding.ActivityMainBinding
import com.taiwanlife.teamwalk.java_utils.CelebrusCSAUtil
import com.taiwanlife.teamwalk.java_utils.DeviceUtil
import com.taiwanlife.teamwalk.remote.HealthConnectRepository
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
import com.taiwanlife.teamwalk.utils.appendCacheBuster
import com.taiwanlife.teamwalk.utils.openHealthConnectSettings
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
import com.taiwanlife.teamwalk.utils.getChromeIntent
import com.taiwanlife.teamwalk.utils.getGson
import com.taiwanlife.teamwalk.utils.quoteJS
import com.taiwanlife.teamwalk.utils.toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.random.Random

class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }),
    ProviderInstaller.ProviderInstallListener,
    MyWebMessageListener.AsyncCallbacks, MyWebView.WebviewLoadingCallback {

    companion object {
        const val GOOGLE_ERROR_DIALOG_REQUEST_CODE = 4

        private const val QUERY_PARAM_TICKET = "ticket"
        private const val KEY_PID = "pid"

        // 0003016: WebView 載入完成監測 timeout 時間（10 秒）
        private const val WEBVIEW_LOAD_TIMEOUT_MS = 10_000L
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
    private var pendingNoInternetAlert: Boolean = false
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    // 0003016: WebView 載入完成監測
    private var webViewLoadTimeoutJob: Job? = null
    private var webViewRebuildAttempted: Boolean = false


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
//        val startTime = System.currentTimeMillis()
//        val riskyApps = getAppsWithOverlayPermission(this)
//
//        if (riskyApps.isNotEmpty()) {
//            val appListString = riskyApps.joinToString(", ")
//            getAlertDialog(
//                context = this,
//                message = "偵測到以下應用程式具有『螢幕覆蓋』權限：\n[$appListString]\n\n為確保您的交易安全，請先關閉或移除這些 App 的懸浮視窗權限。",
//                positiveText = "前往設定",
//                isCancelable = false,
//                shouldShow = true,
//                positiveOnClick = {
//                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//                    startActivity(intent)
//                }
//            )
//        }
//        val endTime = System.currentTimeMillis()
//        val totalTime = endTime - startTime

//        Timber.d(totalTime.toString())
        registerNetworkCallback({
            pendingNoInternetAlert = false
        }) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                // 不在背景 立刻跳就好了
                showNoInternetAlert()
            } else {
                // 在背景 打開flag在OnResume再檢查一次
                pendingNoInternetAlert = true
            }
        }
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
            toLogin()
        }, {
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
            // 在onResume先檢查完資安檢查再進入登入頁
//            toLogin()
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
        observeOnLifeCycle(mainViewModel.landingFlow, onError = {
            toLogin()
        }) { landingResponse ->
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

            // 0003006: 加 cache buster 確保 WebView 不讀快取
            viewBinding.webView.loadUrl(getEnvironmentConfig().webUrl.appendCacheBuster())

            // 0003016: 啟動載入完成監測，等 Web 端透過 JS bridge 呼叫 webviewFinished
            startWebViewLoadingWatcher(getEnvironmentConfig().webUrl)

            handleRedirectIntent(intent)
        }
    }

    override fun onReceivedEvent(eventName: String?, result: String) {
//        super.onReceivedEvent(eventName, result)
        when (eventName) {
            Config.EVENT_NO_ID_TO_LOGIN, Config.EVENT_TO_LOGIN -> {
                toLogin()
            }

            Config.EVENT_LOGIN_SUCCESS_START_LANDING -> {
                mainViewModel.getLanding()
            }

            Config.EVENT_ONBOARDING_CONNECT_COMPLETE_REFRESH_HOME -> {
                viewBinding.webView.reload()
            }
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
        var hostTypeString = uri.lastPathSegment ?: ""
        val hostType = HostTypes.getFromValue(hostTypeString) ?: return

        when (hostType) {
            HOME -> {
//                viewBinding.webView.loadUrl(EnvironmentManager.getEnvironmentConfig().webUrl)
            }

            LOGIN -> {
                // 確保登入重置
                toLogin()
            }

            LOGIN_SUCCESS -> {
                // 如果更改密碼完之後需要繼續執行登入 在onResume裡面做完了
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

    private fun loginFailure() {
        toLogin()
    }

    private fun toOnBoarding(nickName: String, referrerCode: String) {
        val onboardingIntent = PromoteActivity.startPromoteActivity(this, nickName, referrerCode)
        startActivity(onboardingIntent)
    }

    private fun securityCheck() {
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

            if (!isOnline() && pendingNoInternetAlert) {
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

    private fun clearSensitiveData() {
        // 只清除資料 不消除登入狀態
        Utils.clearSensitiveData(this@MainActivity, viewBinding.webView)

        viewBinding.webView.destroy()
//            SensitiveDataUtil.clearWebViewSensitiveData(this, viewBinding.webView, isDestroy)
    }

    private fun toLogin() {
        Utils.clearLoginData(this, viewBinding.webView)

        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        loginLauncher.launch(loginIntent)
    }

    /**
     * 0003016: /landing 後啟動 WebView 載入完成監測
     * 1. 啟動 10s timer，期間若收到 webviewFinished JS bridge → 取消
     * 2. 第一次 timeout → 重建 WebView 並再給 10s
     * 3. 第二次 timeout → 跳「連線異常」dialog → 確認後自動登出
     */
    private fun startWebViewLoadingWatcher(url: String) {
        Timber.d("0003016: 啟動 WebView 載入監測 (10s)")
        cancelWebViewLoadingWatcher()
        webViewRebuildAttempted = false
        webViewLoadTimeoutJob = lifecycleScope.launch {
            delay(WEBVIEW_LOAD_TIMEOUT_MS)
            Timber.w("0003016: 第一次 10s 未收到 webviewFinished → 重建 WebView")
            rebuildWebViewAndRetry(url)
        }
    }

    private fun rebuildWebViewAndRetry(url: String) {
        if (isFinishing || isDestroyed) return

        webViewRebuildAttempted = true

        // 重建 WebView：清掉現有狀態 + 快取 + 重新載入
        viewBinding.webView.stopLoading()
        viewBinding.webView.clearCache(true)
        viewBinding.webView.clearHistory()
        viewBinding.webView.loadUrl(url.appendCacheBuster())

        // 第二次計時
        webViewLoadTimeoutJob = lifecycleScope.launch {
            delay(WEBVIEW_LOAD_TIMEOUT_MS)
            Timber.e("0003016: 第二次 10s 仍未收到 webviewFinished → 跳錯誤 dialog")
            showWebViewLoadFailedDialog()
        }
    }

    private fun showWebViewLoadFailedDialog() {
        if (isFinishing || isDestroyed) return

        AlertDialog.Builder(this)
            .setMessage(R.string.webview_load_timeout_message)
            .setPositiveButton(R.string.confirm1) { _, _ ->
                toLogin()
            }
            .setCancelable(false)
            .show()
    }

    private fun cancelWebViewLoadingWatcher() {
        webViewLoadTimeoutJob?.cancel()
        webViewLoadTimeoutJob = null
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

        if (!viewBinding.webView.url.isNullOrEmpty()
            && viewBinding.webView.url!!.contains(getEnvironmentConfig().webUrlBase)
        ) {
            viewBinding.webView.post {
                viewBinding.webView.evaluateJavascript(
                    "window.WebAppBridge.resumeAPP()",
                    null
                )
            }
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
    }

    override fun onDestroy() {
        clearSensitiveData()

        if (::networkCallback.isInitialized) {
            val cm =
                getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(networkCallback)
        }

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
        lifecycleScope.launch {
            if(healthConnectHelper.checkPermissions()) {
                healthConnectViewModel?.let { healthConnectViewModel ->
                    healthConnectViewModel.getAllData { sleepData, stepsData ->
                        val syncHealthDataModel = SyncHealthDataModel(stepsData, sleepData)

//                val json = Gson().toJson(syncHealthDataModel)
//                Timber.d(json)

                        // 如果有需要透過JS回傳推播設定結果
                        postEvent(EVENT_EXECUTE_JAVASCRIPT_CALLBACK, getGson().toJson(syncHealthDataModel))
                    }
                }
            } else {
                // 如果有需要透過JS回傳推播設定結果
                postEvent(
                    EVENT_EXECUTE_JAVASCRIPT_CALLBACK,
                    getGson().toJson(SyncHealthDataModel(emptyList(), emptyList()))
                )

                getAlertDialog(
                    this@MainActivity,
                    getString(R.string.main_health_connect_permission_revoked),
                    true,
                    true,
                    getString(R.string.confirm2),
                    {
                        // #0003008 對應：點「確認」直接跳到 HC 權限頁
                        openHealthConnectSettings()
                    },
                )
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
        myNotificationManager.updateBadge(
            MyNotificationManager.getPendingIntentForBadge(
                this,
                1003,
                notifyCount
            ), notifyCount
        )
    }

    override fun logout() {
        toLogin()
    }

    /**
     * 0003016: Web 端 bridge 載入完成 → 取消 timeout watcher
     */
    override fun webviewFinished() {
        Timber.d("0003016: 收到 webviewFinished，取消 timeout watcher")
        cancelWebViewLoadingWatcher()
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
//        onLoading(true)
    }

    override fun onWebviewPageFinished() {
//        onLoading(false)
    }

    private fun handleRedirectIntent(intent: Intent) {
        val type = intent.getStringExtra(NOTIFICATION_KEY_TYPE)
        val url = intent.getStringExtra(NOTIFICATION_KEY_URL)
        if (!type.isNullOrEmpty() && !url.isNullOrEmpty()) {
            val notificationType = stringToNotificationType(type)

            when (notificationType) {
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
                                try {
                                    getChromeIntent(url)?.let { intent ->
                                        startActivity(intent)
                                    }
                                } catch (e: ActivityNotFoundException) {
                                    // 如果連 getChromeIntent 找出來的 Intent 都不行
                                    try {
                                        val fallbackIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        startActivity(fallbackIntent)
                                    } catch (e: Exception) {
                                        // 失敗 手機內可能沒有任何瀏覽器
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.no_browser),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            negativeText = getString(R.string.cancel),
                            negativeOnClick = {

                            }
                        )
                    }.show()
                }

                Config.NotificationType.NONE -> {
                    val uri = getEnvironmentConfig().webUrlBase.toUri()
                        .buildUpon()
                        .appendEncodedPath(Config.noneAppPageData.realPath)
                        .build()
                        .toString()
                    // 0003006: 加 cache buster 確保 WebView 不讀快取
                    viewBinding.webView.loadUrl(uri.appendCacheBuster())
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
                    // 0003006: 加 cache buster 確保 WebView 不讀快取
                    viewBinding.webView.loadUrl(uri.appendCacheBuster())
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

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onAvailable()
            }

            override fun onLost(network: Network) {
                onLost()
            }
        }

        cm.registerNetworkCallback(request, networkCallback)
    }

    fun showNoInternetAlert() {

        runOnUiThread {
            if (isFinishing || isDestroyed) {
                return@runOnUiThread
            }

            try {
                pendingNoInternetAlert = false
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
//
//    fun getAppsWithOverlayPermission(context: Context): List<String> {
//        val pm = context.packageManager
//        // 取得所有安裝的 Package，並包含權限資訊
//        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
//        val appsWithPermission = mutableListOf<String>()
//
//        for (pkg in packages) {
//            // 1. 安全取得 ApplicationInfo，若為 null 則跳過
//            val appInfo = pkg.applicationInfo ?: continue
//
//            // 2. 排除系統 App (FLAG_SYSTEM)
//            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
//            if (isSystemApp) continue
//
//            // 3. 安全取得請求的權限清單
//            val requestedPermissions = pkg.requestedPermissions ?: continue
//
//            // 4. 判斷是否申請了覆蓋權限
//            if (requestedPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
//                // 5. 檢查該權限目前是否被使用者開啟
//                if (hasOverlayPermission(context, pkg.packageName)) {
//                    // 使用已經確認非空的 appInfo
//                    val appName = pm.getApplicationLabel(appInfo).toString()
//                    appsWithPermission.add(appName)
//                }
//            }
//        }
//        return appsWithPermission
//    }
//
//    // 檢查特定 Package 是否真的「目前」擁有執行權限
//    private fun hasOverlayPermission(context: Context, packageName: String): Boolean {
//        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//        val mode =
//            appOps.unsafeCheckOpNoThrow(
//                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
//                context.packageManager.getPackageUid(packageName, 0),
//                packageName
//            )
//        return mode == AppOpsManager.MODE_ALLOWED
//    }
}