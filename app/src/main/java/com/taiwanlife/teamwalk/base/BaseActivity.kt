package com.taiwanlife.teamwalk.base

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.databinding.ActivityBaseBinding
import com.taiwanlife.teamwalk.remote.ApiException
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import com.taiwanlife.teamwalk.utils.AlertDialogManager.getAlertDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class BaseActivity<VB : ViewBinding>(private val inflateVB: (LayoutInflater) -> VB) :
    AppCompatActivity() {

    private val sharedEventViewModel: SharedEventViewModel by inject()
    private lateinit var activityBaseBinding: ActivityBaseBinding
    lateinit var viewBinding: VB

    // 是否有提醒過使用者覆蓋
    private var notifyUserOverlay = false

    /**
     * 在子類呼叫時指定 StatusBar 顏色
     * 也會自動設定 StatusBar icon 的亮暗
     */
    @ColorRes
    protected open val statusBarColor: Int = R.color.white

    protected abstract fun onLastCreateBaseActivity(
        view: View,
        savedInstanceState: Bundle?
    )

    /**
     * 有需要的類別自行繼承
     */
    open fun onReceivedEvent(eventName: String?, result: String) {
        if (eventName == Config.EVENT_NO_ID_TO_LOGIN) {
            lifecycleScope.launch {
                delay(200)
                if (!isFinishing && !isDestroyed) {
                    finish()
                }
            }
        }
    }

    /**
     * Loading 用獨立 Window 的 Dialog 顯示。
     *
     * 為什麼不用 BaseActivity 內 loading_container：MainActivity 的全螢幕 WebView 是
     * SurfaceView，走 GPU 直繪 surface 通道，會把同層級的 native View 蓋掉。
     * Dialog 是另一個 Window，比 Activity 自己的 Window 高一層，SurfaceView 蓋不到。
     */
    private var loadingDialog: Dialog? = null
    private var loadingShowJob: Job? = null
    private var loadingDismissJob: Job? = null

    /**
     * 子類覆寫成 true 時，onLoading(true) 不會立刻 show，而是延遲 [LOADING_SHOW_DELAY_MS] 毫秒。
     * 期間若 onLoading(false) 進來會 cancel 掉，loading 根本不會顯示（避免快流程閃爍）。
     *
     * 預設 false（立刻 show）— 適合「已登入直接開」這種主畫面 onCreate 就要立刻顯示的場景。
     * LoginActivity 設 true — 避免登入 API 很快回時 loading 閃一下。
     */
    protected open val showLoadingWithDelay: Boolean = false

    companion object {
        private const val LOADING_SHOW_DELAY_MS = 500L

        // onLoading(false) 觸發後延遲多久才真的關 loading dialog。
        // 期間如果有別的 onLoading(true) 進來會 cancel 掉這次的關閉，loading 持續顯示。
        // 用意是補上 SPA 跳更新對話框 / activity 切換等 gap，避免閃爍。
        private const val LOADING_DISMISS_DELAY_MS = 250L
    }

    private fun getOrCreateLoadingDialog(): Dialog {
        loadingDialog?.let { return it }
        Timber.d("LOADING-DIAG getOrCreate: creating new dialog")
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        dialog.window?.apply {
            // 透明背景（ColorDrawable 比 setBackgroundDrawableResource 穩定）
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            // 撐開全螢幕
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            // 不暗化背景
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            // 點擊穿透：user 可以正常點到 WebView 內 SPA 的對話框 / 按鈕
            // dialog 自己也不吃點擊，但 setCancelable=false 本來就不能點關
            addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        loadingDialog = dialog
        return dialog
    }

    open fun onLoading(loading: Boolean) {
        Timber.d("LOADING-DIAG onLoading($loading) finishing=$isFinishing destroyed=$isDestroyed showLoadingWithDelay=$showLoadingWithDelay")
        if (isFinishing || isDestroyed) return
        val dialog = getOrCreateLoadingDialog()
        try {
            if (loading) {
                // 取消任何 pending dismiss（連續 loading 持續顯示，不閃爍）
                loadingDismissJob?.cancel()
                loadingDismissJob = null
                if (dialog.isShowing) return

                if (showLoadingWithDelay) {
                    // 延遲模式：N 毫秒後才真的 show，期間 onLoading(false) 會 cancel
                    if (loadingShowJob?.isActive == true) return  // 已排程，不要 reset
                    loadingShowJob = lifecycleScope.launch {
                        delay(LOADING_SHOW_DELAY_MS)
                        if (isFinishing || isDestroyed) return@launch
                        if (!dialog.isShowing) {
                            dialog.show()
                            Timber.d("LOADING-DIAG dialog.show() called (delayed)")
                        }
                    }
                } else {
                    // 立刻 show（預設）
                    dialog.show()
                    Timber.d("LOADING-DIAG dialog.show() called (immediate)")
                }
            } else {
                // 取消還沒生效的 show（延遲模式才用得到）
                loadingShowJob?.cancel()
                loadingShowJob = null
                // 延遲 dismiss：期間若再有 onLoading(true) 進來會 cancel 掉
                loadingDismissJob?.cancel()
                loadingDismissJob = lifecycleScope.launch {
                    delay(LOADING_DISMISS_DELAY_MS)
                    if (isFinishing || isDestroyed) return@launch
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        Timber.d("LOADING-DIAG dialog.dismiss() called (after dismiss delay)")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "LOADING-DIAG toggle failed (loading=$loading)")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingShowJob?.cancel()
        loadingShowJob = null
        loadingDismissJob?.cancel()
        loadingDismissJob = null
        try {
            loadingDialog?.takeIf { it.isShowing }?.dismiss()
        } catch (e: Exception) {
            Timber.e(e, "onDestroy dismiss loading failed")
        }
        loadingDialog = null
    }

    /**
     * 雙保險鎖死系統「字型大小」+「顯示大小」設定，避免原生 UI 跑版：
     *   - fontScale = 1.0f                              → 鎖「字型大小」
     *   - densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE  → 鎖「顯示大小」
     *     (DENSITY_DEVICE_STABLE 是裝置出廠原始密度，不被 user 設定影響)
     *
     * 雙保險：
     *   1. attachBaseContext 用 createConfigurationContext 從 base context 直接給乾淨值
     *   2. applyOverrideConfiguration 攔截任何後續系統 / AppCompat 套進來的 override
     *      （night mode、locale、orientation 等情境會觸發）
     *
     * WebView 內網頁文字由 webSettings.textZoom = 100 處理（#0003003），不受這個影響。
     */
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        lockDisplayConfig(config)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.let { lockDisplayConfig(it) }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    /**
     * screenWidthDp / screenHeightDp / smallestScreenWidthDp 是系統用「當下的 density」
     * 從實體 px 除出來的。只改 densityDpi 而不同步換算這三個值，系統就會以為螢幕比實際寬
     * （使用者把「顯示大小」調小時 density 變小、dp 值變大，我們又把 density 拉回原廠值），
     * 導致 DisplayMetrics 算出來的可用寬度大於實體螢幕。
     *
     * 症狀：AlertDialog 的 DecorView 寬度是用這組被污染的 metrics 算的，會超出螢幕右緣，
     * 訊息文字被切掉、「確定」鈕整顆跑到畫面外點不到（0003296 客訴）。
     * 實測 1080x2412 / 480dpi 機器把顯示大小調到 400dpi：dialog 寬 1231px > 螢幕 1080px。
     */
    private fun lockDisplayConfig(config: Configuration) {
        val stableDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
        // 使用者調整「顯示大小」後的實際 density，dp 欄位就是用這個值除出來的
        val userDpi = Resources.getSystem().displayMetrics.densityDpi

        config.fontScale = 1.0f
        config.densityDpi = stableDpi

        if (userDpi <= 0 || userDpi == stableDpi) return

        val ratio = userDpi.toFloat() / stableDpi
        if (config.screenWidthDp > 0) {
            config.screenWidthDp = Math.round(config.screenWidthDp * ratio)
        }
        if (config.screenHeightDp > 0) {
            config.screenHeightDp = Math.round(config.screenHeightDp * ratio)
        }
        if (config.smallestScreenWidthDp > 0) {
            config.smallestScreenWidthDp = Math.round(config.smallestScreenWidthDp * ratio)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 包 try-catch 防 Resources$NotFoundException「failed to redirect ResourcesImpl」
        // 該錯誤通常發生於 OEM ROM 在 App 執行中升級 WebView 元件時，inflate 階段
        // 拿不到 resource → 整個 Activity 起不來。重啟 App 通常即可恢復。
        try {
            activityBaseBinding = ActivityBaseBinding.inflate(layoutInflater)

            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )

            activityBaseBinding.root.filterTouchesWhenObscured = true

            setContentView(activityBaseBinding.root)
            viewBinding = inflateVB.invoke(layoutInflater)
            activityBaseBinding.baseContainer.addView(viewBinding.root)
        } catch (e: Resources.NotFoundException) {
            Timber.e(e, "Inflate failed (Resources/WebView), finishing")
            Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show()
            finishAffinity()
            return
        }

//        window.statusBarColor = ContextCompat.getColor(this, R.color.colorError)
        window.navigationBarColor = Color.BLACK // 不透明，可自行換色

        val isLightStatusBar = isColorLight(statusBarColor)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            // 設定 StatusBar icons 亮暗
            isAppearanceLightStatusBars = isLightStatusBar
            // 確保 NavigationBar icon 保持亮色（白色 icon）
            isAppearanceLightNavigationBars = false
        }


        ViewCompat.setOnApplyWindowInsetsListener(activityBaseBinding.root) { view, insets ->
            val sysBarsInsets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

            view.updatePadding(
                left = sysBarsInsets.left,
                right = sysBarsInsets.right,
                top = 0,
                bottom = sysBarsInsets.bottom
            )
            // API 36以上 改變statusbar顏色會失去效用 使用新方法
            activityBaseBinding.statusBarView.layoutParams.apply {
                height = sysBarsInsets.top
            }
            activityBaseBinding.statusBarView.setBackgroundColor(getColor(statusBarColor))

//            insets
            WindowInsetsCompat.CONSUMED
        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        // 如果需要自定義更詳細的UI在使用下面 暫時觀察 用 enableEdgeToEdge()
        // https://developer.android.com/develop/ui/views/layout/edge-to-edge#kotlin
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//
//        ViewCompat.setOnApplyWindowInsetsListener(activityBaseBinding.root) { view, windowInsets ->
//            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                // 上面能夠繪圖至statusbar下面 下面能夠不被NavigationBar擋住
//                view.setPadding(insets.left, 0, insets.right, 0)
//                this.bottomMargin = insets.bottom
//                this.topMargin = insets.top
//            }
//
//            // Return CONSUMED if you don't want want the window insets to keep being
//            // passed down to descendant views.
//            WindowInsetsCompat.CONSUMED
////            windowInsets
//        }

//        window.statusBarColor = getColor(android.R.color.transparent)
//        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
//            val statusBarHeight =
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    insets.getInsets(WindowInsets.Type.statusBars()).top
//                } else {
//                    @Suppress("DEPRECATION")
//                    insets.systemWindowInsetTop
//                }
//
//            view.setBackgroundColor(getColor(android.R.color.transparent))
//            view.setPadding(0, statusBarHeight, 0, 0)
//            insets
//        }

        observeEvents()

        onLastCreateBaseActivity(viewBinding.root, savedInstanceState)
    }

    fun postEvent(eventName: String?, event: String) {
        sharedEventViewModel.postEvent(event, eventName)
    }

    /**
     * 取得Event的ViewModel 其實也能自己建立 singleton都會拿到同一個
     */
    fun getSharedEventViewModelInstance(): SharedEventViewModel {
        return sharedEventViewModel
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                sharedEventViewModel.eventFlow.collect { (eventName, result) ->
                    onReceivedEvent(eventName, result)
                }
            }
        }
    }

    /**
     * 簡單判斷顏色亮度
     */
    private fun isColorLight(@ColorRes colorResId: Int): Boolean {
        val colorInt = ContextCompat.getColor(this, colorResId)
        val red = Color.red(colorInt)
        val green = Color.green(colorInt)
        val blue = Color.blue(colorInt)

        // 如果是透明，直接定義為「亮色或深色」
        if (Color.alpha(colorInt) == 0) {
            return true // 或 false，看預設透明色偏亮還是偏暗
        }

        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
        return luminance > 128
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val isObscured =
            ev.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0 ||
                    ev.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0

        if (isObscured && !notifyUserOverlay) {
            // 沒有提醒過 且被覆蓋的狀態之下點擊 跳提醒
            notifyUserOverlay = true
            showOverlayWarning()

            return true
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun showOverlayWarning() {
        getAlertDialog(
            context = this,
            message = getString(R.string.main_security_check_overlay),
            icon = R.mipmap.ic_launcher,
            isCancelable = false,
            shouldShow = true,
            positiveText = getString(R.string.understand_and_continue),
            positiveOnClick = {
                // 跳過提醒使用者就能正常使用
//                overlayWarningShown = false
            }
        )
    }


    fun <T> observeOnLifeCycle(
        apiFlowClass: ApiFlowClass<T>,
        lifecycleOwner: LifecycleOwner = this,
        lifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
        onError: (e: Exception) -> Unit = {},
        unSubscribeOnComplete: Boolean = false,
        onSuccess: (T) -> Unit,
    ) {
        lifecycleOwner.lifecycleScope.launch {
            apiFlowClass.getLoadingFlow().collect {
                onLoading(it)
            }
        }
        lifecycleOwner.lifecycleScope.launch {
            val scope = this
            apiFlowClass.getFlow().collect {
                when (it) {
                    UiState.Idle -> {}

                    is UiState.Success<T> -> {
                        onSuccess(it.data)
                        if (unSubscribeOnComplete) {
                            scope.cancel()
                        }
                        onLoading(false)
                    }

                    is UiState.Error -> {
                        // 如果有需要特別處理才會使用
                        onError(it.e)
                        if (unSubscribeOnComplete) {
                            scope.cancel()
                        }

                        when (it.e) {
                            is ApiException.ResponseHeaderCodeNotSuccessException -> {
                                val message = it.e.header.message
                                if (!message.isNullOrEmpty()) {
                                    Toast.makeText(this@BaseActivity, message, Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(
                                        this@BaseActivity,
                                        getString(R.string.general_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is ApiException.ResponseNotSuccessfulException -> {
                                if (it.e.code.toString() == Config.API_CODE_500_LOG_OUT || it.e.code.toString() == Config.API_CODE_401_LOG_OUT) {
                                    postEvent(Config.EVENT_NO_ID_TO_LOGIN, "")
                                }
                                Toast.makeText(
                                    this@BaseActivity,
                                    getString(R.string.general_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    this@BaseActivity,
                                    getString(R.string.general_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        onLoading(false)
                    }
                }
            }
        }
    }
}