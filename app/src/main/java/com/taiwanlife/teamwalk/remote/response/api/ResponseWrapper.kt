package com.taiwanlife.teamwalk.remote.response.api

/**
 * 抽出每次呼叫API都會有的參數 讓我們可以只看我們關注的參數就好
 */
open class ResponseWrapper<T>(
    val header: Header,
    val data: T?
)

data class Header(
    /**
     * 回應代碼(0000代表成功，其餘失敗)
     */
    val code: String,
    /**
     * 系統錯誤訊息
     */
    val message: String,
)