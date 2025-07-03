package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.remote.response.api.Header
import retrofit2.Response

sealed class ApiException(customMessage: String? = null) : Exception(customMessage) {
    data class ResponseNotSuccessfulException(val response: Response<*>, val code: Int) : ApiException()
    data class ResponseHeaderCodeNotSuccessException(val response: Response<*>, val header: Header) : ApiException(header.message)
    data class ResponseBodyEmptyException(val response: Response<*>) : ApiException(Config.API_BODY_EMPTY_MESSAGE)
}