/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/12/15
 */
public interface SysConfigService {

    /**
     *
     * @return
     */
    @GET("sys/get_config")
    Call<Map> getAppVersion();
}
