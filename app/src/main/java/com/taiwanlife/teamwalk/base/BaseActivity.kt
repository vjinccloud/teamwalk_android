package com.taiwanlife.teamwalk.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import androidx.lifecycle.withStarted
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.MaterialColors.isColorLight
import com.google.gson.Gson
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.databinding.ActivityBaseBinding
import com.taiwanlife.teamwalk.remote.ApiException
import com.taiwanlife.teamwalk.ui.common.SharedEventViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class BaseActivity<VB : ViewBinding>(private val inflateVB: (LayoutInflater) -> VB) :
    AppCompatActivity() {

    private val sharedEventViewModel: SharedEventViewModel by inject()
    private lateinit var activityBaseBinding: ActivityBaseBinding
    lateinit var viewBinding: VB

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
        if(eventName == Config.EVENT_NO_TOKEN_TO_LOGIN) {
            lifecycleScope.launch {
                delay(200)
                if(!isFinishing && !isDestroyed) {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        activityBaseBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(activityBaseBinding.root)
        viewBinding = inflateVB.invoke(layoutInflater)
        activityBaseBinding.baseContainer.addView(viewBinding.root)

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

    fun <T> observeOnLifeCycle(
        sharedFlow: SharedFlow<UiState<T>>,
        lifecycleOwner: LifecycleOwner = this,
        lifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
        onError: (e: Exception) -> Unit = {},
        unSubscribeOnComplete: Boolean = false,
        onSuccess: (T) -> Unit,
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(lifeCycleState) {
                val scope = this
                sharedFlow.collect {
                    when (it) {
                        UiState.Loading -> {
                            //
                        }

                        is UiState.Success<T> -> {
                            onSuccess(it.data)
                            if (unSubscribeOnComplete) {
                                scope.cancel()
                            }
                        }

                        is UiState.Error -> {
                            // 如果有需要特別處理才會使用
                            onError(it.e)
                            if(BuildConfig.DEBUG) {
                                when(it.e) {
                                    is ApiException.ResponseHeaderCodeNotSuccessException -> {
                                        Toast.makeText(this@BaseActivity, it.e.header.message, Toast.LENGTH_SHORT).show()
                                    }
                                    is ApiException.ResponseNotSuccessfulException -> {
                                        val errorBody = it.e.response.errorBody()
                                        Toast.makeText(this@BaseActivity, it.e.code.toString() + " - " + errorBody?.string(), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            if (unSubscribeOnComplete) {
                                scope.cancel()
                            }
                            when(it.e) {
                                is ApiException.ResponseHeaderCodeNotSuccessException -> {
                                    if(it.e.header.code == Config.API_CODE_NO_TOKEN) {
                                        postEvent(Config.EVENT_NO_TOKEN_TO_LOGIN, "")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}