package com.taiwanlife.teamwalk.utils

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

class WebViewFileWrapper(
    private var filePathCallbackParam: ValueCallback<Array<Uri>>?,
    private val fileChooserParams: WebChromeClient.FileChooserParams
) {
    fun onReceiveValue(uris: Array<Uri>?) {
        if(uris == null || uris.isEmpty()) {
            filePathCallbackParam?.onReceiveValue(null)
            filePathCallbackParam = null
        } else {
            filePathCallbackParam?.onReceiveValue(uris)
        }
    }
}