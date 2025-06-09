package com.taiwanlife.teamwalk.service;

import com.taiwanlife.teamwalk.model.request.LoginRequest;
import com.taiwanlife.teamwalk.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginService {

    @POST("teamwalk-fe-api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

}
