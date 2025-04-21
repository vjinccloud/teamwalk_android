package com.taiwanlife.teamwalk.model;

/**
 * Author : Ryans
 * Date : 2021/5/19
 * Introduction :
 */
public class Pattern {

    private String userId;
    private String newPatternLock;
    private String origPatternLock;

    public Pattern(String userId, String newPatternLock, String origPatternLock) {
        this.userId = userId;
        this.newPatternLock = newPatternLock;
        this.origPatternLock = origPatternLock;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewPatternLock() {
        return newPatternLock;
    }

    public void setNewPatternLock(String newPatternLock) {
        this.newPatternLock = newPatternLock;
    }

    public String getOrigPatternLock() {
        return origPatternLock;
    }

    public void setOrigPatternLock(String origPatternLock) {
        this.origPatternLock = origPatternLock;
    }
}
