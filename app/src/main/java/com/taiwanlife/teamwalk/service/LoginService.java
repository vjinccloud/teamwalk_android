package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.request.LoginRequest;
import com.taiwanlife.teamwalk.model.response.LoginResponse;
import com.taiwanlife.teamwalk.model.response.UserInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Header;

public interface LoginService {

    @GET("teamwalk-fe-api/user-info")
    Call<UserInfoResponse> getUserInfo(@Header("authorization") String jwt);

    @POST("teamwalk-fe-api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

}
