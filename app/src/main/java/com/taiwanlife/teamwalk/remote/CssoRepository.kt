package com.taiwanlife.teamwalk.remote

import com.taiwanlife.teamwalk.remote.service.CssoService
import retrofit2.Response

class CssoRepository(
    private val cssoService: CssoService
) {
    /**
     * 模擬CSSO
     */
    suspend fun cssoLogin(
        sysId: String,
        applId: String,
        applPwd: String,
        service: String,
    ): Response<String> {
        return cssoService.cssoLogin(sysId, applId, applPwd, service)
    }

    suspend fun cssoPatternLogin(
        sysId: String,
        userId: String,
        patternPath: String,
        service: String,
    ): Response<String> {
        return cssoService.cssoPatternLogin(sysId, userId, patternPath, service)
    }
}