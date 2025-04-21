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
public class UserAvatar {
    private String buildinImage;
    private String customImage;
    private String filter;

    public UserAvatar(String buildinImage, String customImage, String filter) {
        this.buildinImage = buildinImage;
        this.customImage = customImage;
        this.filter = filter;
    }

    public String getBuildinImage() {
        return buildinImage;
    }

    public void setBuildinImage(String buildinImage) {
        this.buildinImage = buildinImage;
    }

    public String getCustomImage() {
        return customImage;
    }

    public void setCustomImage(String customImage) {
        this.customImage = customImage;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
