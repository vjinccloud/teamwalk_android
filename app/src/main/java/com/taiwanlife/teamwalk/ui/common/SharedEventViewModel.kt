package com.taiwanlife.teamwalk.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SharedEventViewModel : ViewModel() {
    private val _eventFlow = MutableSharedFlow<Pair<String?, String>>(extraBufferCapacity = 100)
    val eventFlow: SharedFlow<Pair<String?, String>> = _eventFlow.asSharedFlow()

    fun postEvent(event: String, eventName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _eventFlow.emit(eventName to event)
        }
    }
}