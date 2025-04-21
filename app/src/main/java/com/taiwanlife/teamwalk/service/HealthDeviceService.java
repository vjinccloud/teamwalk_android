/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.HealthDevice;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/3
 */
public interface HealthDeviceService {

    /**
     *
     * @param authorization
     * @param healthDevice
     * @return
     */
    @POST("device")
    Call<Map> bindHealthDevice(@Header("Authorization") String authorization,
                               @Body HealthDevice healthDevice);

    /**
     *
     * @param authorization
     * @return
     */
    @POST("device/deleteAll")
    Call<Map> removeHealthDevices(@Header("Authorization") String authorization, @Body Map<String, Object> noimei);
}
