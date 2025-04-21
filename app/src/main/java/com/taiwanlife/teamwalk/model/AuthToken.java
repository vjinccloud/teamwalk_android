/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.model;

import java.util.Date;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/3
 */
public class AuthToken {
    private int code;
    private String message;
    private String status;
    private Date timestamp;
    private String token;
    private String refreshToken;
    private long expireTime;

    public AuthToken(int code, String message, String status, Date timestamp, String token, String refreshToken, long expireTime) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expireTime = expireTime;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
