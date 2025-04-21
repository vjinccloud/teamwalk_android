/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.model;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/3
 */
public class HealthDevice {
    private String type;
    private String token;
    private String secret;

    public HealthDevice(String type, String token, String secret) {
        this.type = type;
        this.token = token;
        this.secret = secret;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
