/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.model;

import java.util.Date;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/11
 */
public class HealthKitBind {
    private String deviceType;
    private Date bindAt;

    public HealthKitBind(String deviceType, Date bindAt) {
        this.deviceType = deviceType;
        this.bindAt = bindAt;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Date getBindAt() {
        return bindAt;
    }

    public void setBindAt(Date bindAt) {
        this.bindAt = bindAt;
    }
}
