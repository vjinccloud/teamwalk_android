package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.Pattern;
import com.taiwanlife.teamwalk.model.PatternDisableBody;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Author : Ryans
 * Date : 2021/5/19
 * Introduction :
 */
public interface PatternService {


    @POST("rest/setPatternLock")
    Call<Map<String, String>> setPatternLock(@Header("Cookie") String Cookie,
                                      @Body Pattern pattern);

    @POST("user/disable_graphical_login")
    Call<Map<String, Object>> disablePattern(@Body PatternDisableBody patternDisableBody);


}
