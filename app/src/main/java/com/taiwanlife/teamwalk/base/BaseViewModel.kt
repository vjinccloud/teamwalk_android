package com.taiwanlife.teamwalk.base

import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.remote.ApiException.ResponseBodyEmptyException
import com.taiwanlife.teamwalk.remote.ApiException.ResponseHeaderCodeNotSuccessException
import com.taiwanlife.teamwalk.remote.ApiException.ResponseNotSuccessfulException
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.api.ResponseWrapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber

/**
 * 會傳到UI層的State
 * 根據State決定現在UI如何顯示
 * Loading -> 正在打API 可以用來顯示Loading
 * Success -> 成功 後續處理和關閉Loading
 * Error -> 錯誤 後續處理和關閉Loading 注意這邊我們有透過errorHandler 處理錯誤了 UI層如果要另外再處理注意不要做重複的工
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val e: Exception) : UiState<Nothing>()
}


abstract class BaseViewModel(
    protected val repository: Repository
) : ViewModel() {

    /**
     * 傳入要呼叫的API function
     */
    class ApiFlow<T>(private val baseViewModel: BaseViewModel) {
        private val _mutableSharedFlow = MutableSharedFlow<UiState<T>>()
        val sharedFlow get() = _mutableSharedFlow.asSharedFlow()

        fun execute(
            errorHandler: ((e: Exception, defaultErrorHandler: (e: Exception) -> Unit) -> Unit)? = null,
            executeCall: suspend () -> Response<ResponseWrapper<T>>
        ) {
            baseViewModel.apiCallAsSharedFlow(_mutableSharedFlow, executeCall, errorHandler)
        }
    }

    /**
     * 傳入要呼叫的Raw function
     */
    class RawFlow<T>(private val baseViewModel: BaseViewModel) {
        private val _mutableSharedFlow = MutableSharedFlow<UiState<T>>()
        val sharedFlow get() = _mutableSharedFlow.asSharedFlow()

        fun execute(
            errorHandler: ((e: Exception, defaultErrorHandler: (e: Exception) -> Unit) -> Unit)? = null,
            executeCall: suspend () -> Response<T>
        ) {
            baseViewModel.rawCallAsSharedFlow(_mutableSharedFlow, executeCall, errorHandler)
        }
    }

    /**
     * 傳入要呼叫的CSSO function
     */
    class CSSOFlow(private val baseViewModel: BaseViewModel) {
        private val _mutableSharedFlow = MutableSharedFlow<UiState<String>>()
        val sharedFlow get() = _mutableSharedFlow.asSharedFlow()

        fun execute(
            errorHandler: ((e: Exception, defaultErrorHandler: (e: Exception) -> Unit) -> Unit)? = null,
            executeCall: suspend () -> Response<String>
        ) {
            baseViewModel.cssoCallAsSharedFlow(_mutableSharedFlow, executeCall, errorHandler)
        }
    }

    /**
     * 呼叫API的function
     * 如果傳入errorHandler 則這次呼叫API的錯誤會自行處理 會將default也傳入 方便在處理完自己需要的錯誤後 其他丟回給Default
     */
    private fun <T> apiCallAsSharedFlow(
        mutableSharedFlow: MutableSharedFlow<UiState<T>>,
        apiFunction: suspend () -> Response<ResponseWrapper<T>>,
        errorHandler: ((e: Exception, defaultErrorHandler: (e: Exception) -> Unit) -> Unit)? = null
    ) {
        val processError = { e: Exception ->
            if (errorHandler != null) {
                errorHandler(e, ::defaultErrorHandler)
            } else {
                defaultErrorHandler(e)
            }
        }
        viewModelScope.launch {
            mutableSharedFlow.emit(UiState.Loading)
            try {
                val response = apiFunction()
                // 看API是不是[200, 300)
                if (response.isSuccessful) {
                    // 看Body是不是空的
                    if (response.body() != null) {
                        val body = response.body()!!
                        // 看body裡面的header是不是回傳正確的code 0000
                        if (body.header.code == Config.API_CODE_SUCCESS) {
                            // 到這裡都正確
                            // 如果我們期待的回傳值是Unit 代表我們期待這裡的data是null 這狀況將他轉為Unit
                            val data = body.data ?: Unit as T
                            mutableSharedFlow.emit(UiState.Success(data))
                        } else {
                            // 回傳code 不是0000 帶message給他
                            val e =
                                ResponseHeaderCodeNotSuccessException(response, body.header)
                            processError(e)
                            mutableSharedFlow.emit(UiState.Error(e))
                        }
                    } else {
                        // body是空的
                        val e = ResponseBodyEmptyException(response)
                        processError(e)
                        mutableSharedFlow.emit(UiState.Error(e))
                    }
                } else {
                    // HttpCode不是 [200,300) 通常代表錯誤
                    val e = ResponseNotSuccessfulException(response, response.code())
                    processError(e)
                    mutableSharedFlow.emit(UiState.Error(e))
                }

            } catch (e: Exception) {
                processError(e)
                mutableSharedFlow.emit(UiState.Error(e))
            }
        }
    }

    private fun <T> rawCallAsSharedFlow(
        mutableSharedFlow: MutableSharedFlow<UiState<T>>,
        apiFunction: suspend () -> Response<T>,
        errorHandler: ((e: Exception, defaultErrorHandler: (e: Exception) -> Unit) -> Unit)? = null
    ) {
        val processError = { e: Exception ->
            if (errorHandler != null) {
                errorHandler(e, ::defaultErrorHandler)
            } else {
                defaultErrorHandler(e)
            }
        }
        viewModelScope.launch {
            mutableSharedFlow.emit(UiState.Loading)
            try {
                val response = apiFunction.invoke()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        mutableSharedFlow.emit(UiState.Success(body))
                    } else {
                        // body是空的
                        val e = ResponseBodyEmptyException(response)
                        processError(e)
                        mutableSharedFlow.emit(UiState.Error(e))
                    }
                } else {
                    // HttpCode不是 [200,300) 通常代表錯誤
                    val e = ResponseNotSuccessfulException(response, response.code())
                    processError(e)
                    mutableSharedFlow.emit(UiState.Error(e))
                }
            } catch (e: Exception) {
                processError(e)
                mutableSharedFlow.emit(UiState.Error(e))
            }
        }
    }

    private fun cssoCallAsSharedFlow(
        mutableSharedFlow: MutableSharedFlow<UiState<String>>,
        apiFunction: suspend () -> Response<String>,
        errorHandler: ((e: Exception, defaultErrorHandler: (e: Exception) -> Unit) -> Unit)? = null
    ) {
        val processError = { e: Exception ->
            if (errorHandler != null) {
                errorHandler(e, ::defaultErrorHandler)
            } else {
                defaultErrorHandler(e)
            }
        }
        viewModelScope.launch {
            mutableSharedFlow.emit(UiState.Loading)
            try {
                val response = apiFunction.invoke()
                // CSSO 比較特別 如果成功HttpCode 會是302 並且將我們要的東西放在Header的location
                if (response.code() == 302) {
                    val location = response.headers()[Config.API_CSSO_HEADER_LOCATION]
                    if (!TextUtils.isEmpty(location)) {
                        mutableSharedFlow.emit(UiState.Success(location!!))
                    } else {
                        // location 是空的
                        val e = ResponseBodyEmptyException(response)
                        processError(e)
                        mutableSharedFlow.emit(UiState.Error(e))
                    }
                } else {
                    // HttpCode不是 302 通常代表錯誤
                    val e = ResponseNotSuccessfulException(response, response.code())
                    processError(e)
                    mutableSharedFlow.emit(UiState.Error(e))
                }
            } catch (e: Exception) {
                processError(e)
                mutableSharedFlow.emit(UiState.Error(e))
            }
        }
    }

    /**
     * 如果override此方法 則整個ViewModel的錯誤處理都會使用override
     */
    fun defaultErrorHandler(e: Exception) {
        when (e) {
            is ResponseNotSuccessfulException -> {
                Timber.e(e)
            }

            is ResponseHeaderCodeNotSuccessException -> {
                if (e.header.code == Config.API_CODE_NO_TOKEN) {
                    // 在UI層的錯誤處理做登出
                }
                Timber.e(e)
            }

            is ResponseBodyEmptyException -> {
                Timber.e(e)
            }

            else -> {
                Timber.e(e)
            }
        }
    }
}