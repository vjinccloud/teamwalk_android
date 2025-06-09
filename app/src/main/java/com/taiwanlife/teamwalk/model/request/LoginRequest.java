package com.taiwanlife.teamwalk.model.request;

import androidx.annotation.NonNull;

public class LoginRequest {

    private String ticket;
    private String service;
    private String app_uuid;
    private String device_id;
    private String push_id;

    public LoginRequest(String ticket, String service, String app_uuid, String device_id, String push_id) {
        this.ticket = ticket;
        this.service = service;
        this.app_uuid = app_uuid;
        this.device_id = device_id;
        this.push_id = push_id;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getApp_uuid() {
        return app_uuid;
    }

    public void setApp_uuid(String app_uuid) {
        this.app_uuid = app_uuid;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoginRequest{" +
                "ticket='" + ticket + '\'' +
                ", service='" + service + '\'' +
                ", app_uuid='" + app_uuid + '\'' +
                ", device_id='" + device_id + '\'' +
                ", push_id='" + push_id + '\'' +
                '}';
    }
}
