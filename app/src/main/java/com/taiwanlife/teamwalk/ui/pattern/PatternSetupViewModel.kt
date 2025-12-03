package com.taiwanlife.teamwalk.ui.pattern

import androidx.lifecycle.viewModelScope
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.CSSOResponse
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import kotlinx.coroutines.launch

class PatternSetupViewModel(repository: Repository): BaseViewModel(repository) {
    val patternFlow = CSSOFlow<CSSOResponse>(this)

    fun testPattern(origPattern: String, newPattern: String) {
        val castGC = SecuredPreferenceStoreManager.getString(Config.SP_CASTGC, "")
        val userName = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_REMEMBER_PID, "")

        viewModelScope.launch {
            patternFlow.execute { repository.cssoRepository.setPatternLock(castGC, userName, origPattern, newPattern) }
        }
    }

    fun disablePattern() {
        val pid = SecuredPreferenceStoreManager.getString(Config.SP_LOGIN_REMEMBER_PID, "")

        viewModelScope.launch {
            patternFlow.execute { repository.cssoRepository.disablePatternLock(pid) }
        }
    }
}