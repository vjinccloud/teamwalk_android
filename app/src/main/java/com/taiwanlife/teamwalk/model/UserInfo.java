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
public class UserInfo {
    private String loginMethod;
    private String ticket;
    private String castgc;

    private String id;
    private String nickName;
    private String gender;
    private float height;
    private float weight;
    private String mobile;
    private String email;
    private String city;
    private String district;
    private String companyID;
    private String companyTID;
    private String companyName;
    private String b2b;
    private String referrerCode;
    private String workoutInterval;
    private boolean completeOnboarding;
    private boolean enableNotification;

    public static final String DEVICE_OS = "android";
    private String deviceOS;
    private String deviceToken;

    private UserAvatar avatar;
    private HealthKitBind healthKitBind;
    private HealthStatus healthStatus;

    private int level;
    private int coins;

    private boolean manualReturn;

    public UserInfo(String loginMethod, String ticket, String castgc, String id, String nickName, String gender, float height, float weight, String mobile, String email, String city, String district, String companyID, String companyTID, String companyName, String b2b, String referrerCode, String workoutInterval, boolean completeOnboarding, boolean enableNotification, String deviceOS, String deviceToken, UserAvatar avatar, HealthKitBind healthKitBind, HealthStatus healthStatus, int level, int coins, boolean manualReturn) {
        this.loginMethod = loginMethod;
        this.ticket = ticket;
        this.castgc = castgc;
        this.id = id;
        this.nickName = nickName;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.mobile = mobile;
        this.email = email;
        this.city = city;
        this.district = district;
        this.companyID = companyID;
        this.companyTID = companyTID;
        this.companyName = companyName;
        this.b2b = b2b;
        this.referrerCode = referrerCode;
        this.workoutInterval = workoutInterval;
        this.completeOnboarding = completeOnboarding;
        this.enableNotification = enableNotification;
        this.deviceOS = deviceOS;
        this.deviceToken = deviceToken;
        this.avatar = avatar;
        this.healthKitBind = healthKitBind;
        this.healthStatus = healthStatus;
        this.level = level;
        this.coins = coins;
        this.manualReturn = manualReturn;
    }

    public String getLoginMethod() {
        return loginMethod;
    }

    public void setLoginMethod(String loginMethod) {
        this.loginMethod = loginMethod;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getCastgc() {
        return castgc;
    }

    public void setCastgc(String castgc) {
        this.castgc = castgc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getCompanyTID() {
        return companyTID;
    }

    public void setCompanyTID(String companyTID) {
        this.companyTID = companyTID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getB2b() { return b2b; }

    public void setB2b(String b2b) { this.b2b = b2b; }

    public String getReferrerCode() {
        return referrerCode;
    }

    public void setReferrerCode(String referrerCode) {
        this.referrerCode = referrerCode;
    }

    public String getWorkoutInterval() {
        return workoutInterval;
    }

    public void setWorkoutInterval(String workoutInterval) {
        this.workoutInterval = workoutInterval;
    }

    public boolean isCompleteOnboarding() {
        return completeOnboarding;
    }

    public void setCompleteOnboarding(boolean completeOnboarding) {
        this.completeOnboarding = completeOnboarding;
    }

    public boolean isEnableNotification() {
        return enableNotification;
    }

    public void setEnableNotification(boolean enableNotification) {
        this.enableNotification = enableNotification;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceToken() { return deviceToken; }

    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

    public UserAvatar getAvatar() {
        return avatar;
    }

    public void setAvatar(UserAvatar avatar) {
        this.avatar = avatar;
    }

    public HealthKitBind getHealthKitBind() {
        return healthKitBind;
    }

    public void setHealthKitBind(HealthKitBind healthKitBind) {
        this.healthKitBind = healthKitBind;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public boolean isManualReturn() {
        return manualReturn;
    }

    public void setManualReturn(boolean manualReturn) {
        this.manualReturn = manualReturn;
    }
}
