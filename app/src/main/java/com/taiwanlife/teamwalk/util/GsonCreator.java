/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/11/6
 */
public class GsonCreator {

    /**
     *
     * @return
     */
    public static Gson build(DateFormat dateFormat) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            @Override
            public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
                    throws JsonParseException {
                try {
                    return dateFormat.parse(json.getAsString());
                } catch (ParseException e) {
                    return null;
                }
            }
        });

        return gsonBuilder.create();
    }
}
