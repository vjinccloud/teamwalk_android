/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.model;

import java.util.Date;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class Annoucement {
    private String title;
    private String message;
    private String image;
    private boolean lockLogin;
    private Date lockStart;
    private Date lockEnd;

    /**
     *
     * @param title
     * @param message
     * @param image
     * @param lockLogin
     * @param lockStart
     * @param lockEnd
     */
    public Annoucement(String title, String message, String image, boolean lockLogin, Date lockStart, Date lockEnd) {
        this.title = title;
        this.message = message;
        this.image = image;
        this.lockLogin = lockLogin;
        this.lockStart = lockStart;
        this.lockEnd = lockEnd;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public boolean isLockLogin() {
        return lockLogin;
    }

    public void setLockLogin(boolean lockLogin) {
        this.lockLogin = lockLogin;
    }

    public Date getLockStart() {
        return lockStart;
    }

    public void setLockStart(Date lockStart) {
        this.lockStart = lockStart;
    }

    public Date getLockEnd() {
        return lockEnd;
    }

    public void setLockEnd(Date lockEnd) {
        this.lockEnd = lockEnd;
    }
}
