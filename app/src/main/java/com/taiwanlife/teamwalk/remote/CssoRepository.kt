package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.remote.request.DisablePatternRequest
import com.taiwanlife.teamwalk.remote.request.PatternRequest
import com.taiwanlife.teamwalk.remote.response.CSSOResponse
import com.taiwanlife.teamwalk.remote.service.CssoService
import retrofit2.Response
import retrofit2.http.Field

class CssoRepository(
    private val cssoService: CssoService
) {
    /**
     * 模擬CSSO
     * @param sysId SYS_ID
     * @param applId appl_id
     * @param applPwd appl_pwd
     * @param service service
     */
    suspend fun cssoLogin(
        sysId: String,
        applId: String,
        applPwd: String,
        service: String
    ): Response<String> {
        return cssoService.cssoLogin(sysId, applId, applPwd, service)
    }

    suspend fun patternLogin(
        sysId: String,
        userId: String,
        patternPath: String,
        service: String
    ): Response<String> {
        return cssoService.patternLogin(sysId, userId, patternPath, service)
    }

    suspend fun setPatternLock(castgc: String, userName: String, origPattern: String, newPattern: String): Response<CSSOResponse> {
        return cssoService.setPatternLock("CASTGC=$castgc",
            PatternRequest(userName, newPattern, origPattern)
        )
    }

    suspend fun disablePatternLock(pid: String): Response<CSSOResponse> {
        return cssoService.disablePatternLock(DisablePatternRequest(pid))
    }
}