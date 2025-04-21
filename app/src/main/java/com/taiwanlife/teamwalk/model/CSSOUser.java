/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.model;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class CSSOUser {

    private String rspCode;

    private String rspMsg;

    private String uid;

    private String patternLockStatus;

    /**
     *
     * @param rspCode
     * @param rspMsg
     * @param uid
     * @param patternLockStatus
     */
    public CSSOUser(String rspCode, String rspMsg, String uid, String patternLockStatus) {
        this.rspCode = rspCode;
        this.rspMsg = rspMsg;
        this.uid = uid;
        this.patternLockStatus = patternLockStatus;
    }

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public void setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPatternLockStatus() {
        return patternLockStatus;
    }

    public void setPatternLockStatus(String patternLockStatus) {
        this.patternLockStatus = patternLockStatus;
    }
}
