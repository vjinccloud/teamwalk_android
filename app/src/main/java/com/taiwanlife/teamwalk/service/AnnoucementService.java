/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.Annoucement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public interface AnnoucementService {

    public static final String ANNOUCEMENT = "s";
    public static final String WELCOME = "w";
    public static final String BULLETIN = "b";

    /**
     *
     * @param type
     * @return
     */
    @GET("sys/announcement")
    Call<List<Annoucement>> listAnnoucements(@Query("type") String type);

    /**
     *
     * @param type
     * @return
     */
    @GET("sys/announcement")
    Call<Annoucement> getWelcome(@Query(("type")) String type);
}
