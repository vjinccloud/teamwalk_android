package com.taiwanlife.teamwalk.remote

/**
 * 由於專案本身不複雜 就不Mapping Dto和Model 只使用Model 直接對應APIResponse和正常UI使用
 * 但是由於有多個Remote : API/CSSO/Garmin/Google
 * 這裡做為API的起始點 呼叫時提供各Repository的入口
 */
class Repository(
    val api: ApiRepository,
    val cssoRepository: CssoRepository,
    val garmin: GarminRepository,
    val fitbit: FitbitRepository,
)