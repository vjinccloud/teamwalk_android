package com.taiwanlife.teamwalk.ui.main

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.viewModelScope
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.api.LandingResponse
import com.taiwanlife.teamwalk.remote.response.api.SysParamInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(repository: Repository) : BaseViewModel(repository) {
    val userInfoFlow = ApiFlow<UserInfoResponse>(this)
    val landingFlow = ApiFlow<LandingResponse>(this)
    val systemParamFlow = ApiFlow<SysParamInfoResponse>(this)

    fun getUserInfo() {
        userInfoFlow.execute {
            repository.api.getUserInfo()
        }
    }

    fun getLanding() {
        landingFlow.execute(true) {
            repository.api.getLanding()
        }
    }

    fun getSysParam() {
        systemParamFlow.execute(true) {
            repository.api.getSysParam()
        }
    }

    fun startToWriteFile(
        contentResolver: ContentResolver,
        uri: Uri,
        data: String,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                val byte = Base64.decode(data.replaceFirst("data:text/plain;base64,", ""), 0)
                contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(byte)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

            withContext(Dispatchers.Main) {
                onDone(result)
            }
        }
    }
}