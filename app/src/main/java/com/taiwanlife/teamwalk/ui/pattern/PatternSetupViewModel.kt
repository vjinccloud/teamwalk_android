package com.taiwanlife.teamwalk.ui.pattern

import androidx.lifecycle.viewModelScope
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.base.BaseViewModel
import com.taiwanlife.teamwalk.remote.Repository
import com.taiwanlife.teamwalk.remote.response.CSSOResponse
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import kotlinx.coroutines.launch

class PatternSetupViewModel(repository: Repository) : BaseViewModel(repository) {
    val patternFlow = CSSOFlow<CSSOResponse>(this)

    fun setPatternLock(castGC: String, userName: String, origPattern: String, newPattern: String) {

        viewModelScope.launch {
            patternFlow.execute {
                repository.cssoRepository.setPatternLock(
                    castGC, userName, origPattern, newPattern
                )
            }
        }
    }

    fun disablePattern() {
        val pid = SecuredPreferenceStoreManager.getString(Config.SP_PID, "")

        viewModelScope.launch {
            patternFlow.execute { repository.cssoRepository.disablePatternLock(pid) }
        }
    }
}