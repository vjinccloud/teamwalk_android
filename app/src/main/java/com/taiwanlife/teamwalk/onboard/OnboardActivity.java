/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.onboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.speed_trap.android.WebAppInterface;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.login.CSSOWebViewActivity;
import com.taiwanlife.teamwalk.model.AuthToken;
import com.taiwanlife.teamwalk.model.UserInfo;
import com.taiwanlife.teamwalk.service.AuthenticateService;
import com.taiwanlife.teamwalk.service.UserService;
import com.taiwanlife.teamwalk.util.CelebrusCSAUtil;
import com.taiwanlife.teamwalk.util.DeviceUtil;
import com.taiwanlife.teamwalk.util.GsonCreator;
import com.taiwanlife.teamwalk.util.SensitiveDataUtil;
import com.taiwanlife.teamwalk.util.Utilities;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/12/3
 */
public class OnboardActivity extends AppCompatActivity {

    private static final String TAG = "OnboardActivity";
    private static final String APP = "Teamwalk";
    private String uname = "";
    private String tk ="";
    private String t ="";
    private String rt ="";
    private long exp = 0L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceUtil.setFlagSecure(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearSensitiveData(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        restoreSensitiveData();
//        CelebrusCSAUtil.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        backupSensitiveData();
        clearSensitiveData(false);
    }

    private void backupSensitiveData(){
        SecuredPreferenceStore pref = SecuredPreferenceStore.getSharedInstance();
        SecuredPreferenceStore.Editor editor = pref.edit();
        editor.putString(getString((R.string.pref_login_username)), uname);
        editor.putString(getString(R.string.pref_login_ticket), tk);
        editor.putString(getString(R.string.pref_login_token), t);
        editor.putString(getString(R.string.pref_login_refresh_token), rt);
        editor.putLong(getString(R.string.pref_login_exp), exp);
    }
    private void restoreSensitiveData(){
        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
        uname = loginSharedPref.getString(getString(R.string.pref_login_username), "");
        tk = loginSharedPref.getString(getString(R.string.pref_login_ticket), "");
        t = loginSharedPref.getString(getString(R.string.pref_login_token), "");
        rt = loginSharedPref.getString(getString(R.string.pref_login_refresh_token), "");
        exp = loginSharedPref.getLong(getString(R.string.pref_login_exp), 0L);
    }
    private void clearSensitiveData(boolean isDestroy) {
        if (uname != null) {
            Arrays.fill(uname.toCharArray(), '\0');
            uname = null;
        }
        if (tk != null) {
            Arrays.fill(tk.toCharArray(), '\0');
            tk = null;
        }
        if (t != null) {
            Arrays.fill(t.toCharArray(), '\0');
            t = null;
        }
        if (rt != null) {
            Arrays.fill(rt.toCharArray(), '\0');
            rt = null;
        }
        exp = 0L;
    }

    private static UserInfo myUser;

    public void loaduser() {
//        SharedPreferences loginSharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), OnboardActivity.MODE_PRIVATE);
        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
        uname = loginSharedPref.getString(getString(R.string.pref_login_username), "");
        tk = loginSharedPref.getString(getString(R.string.pref_login_ticket), "");
        t = loginSharedPref.getString(getString(R.string.pref_login_token), "");
        rt = loginSharedPref.getString(getString(R.string.pref_login_refresh_token), "");
        exp = loginSharedPref.getLong(getString(R.string.pref_login_exp), 0L);
    }

    public void getUser() {
        loaduser();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))))
                .client(okHttpClient)
                .build();

        UserService userService = retrofit.create(UserService.class);

        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        expire.setTimeInMillis(exp);
        if (now.before(expire)) {
            Call<UserInfo> getUerInfoCall = userService.getUserInfo(t);
            getUerInfoCall.enqueue(new Callback<UserInfo>() {

                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    UserInfo userInfo = (UserInfo) response.body();
                    if (userInfo == null) {
                        Log.d(TAG, "get user went wrong");
                    } else {
                        Log.i(TAG, "done getting user");
//                        SecuredPreferenceStore loginSharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), OnboardActivity.MODE_PRIVATE);
                        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
                        String castgc = loginSharedPref.getString(getString(R.string.pref_login_castgc), "");
                        userInfo.setCastgc(castgc);
                        myUser = userInfo;
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    Log.d(TAG, "get user fail");
                }
            });
        } else {
            RequestBody formBody = new FormBody.Builder()
                    .add("username", uname)
                    .add("token", t)
                    .add("refreshToken", rt)
                    .build();

            Retrofit authRetrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.api_url))
                    .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))))
                    .build();

            AuthenticateService authenticateService = authRetrofit.create(AuthenticateService.class);
            Call<AuthToken> authTokenCall = authenticateService.authToken(getString(R.string.origin), formBody);
            authTokenCall.enqueue(new Callback<AuthToken>() {
                @Override
                public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                    AuthToken authToken = (AuthToken) response.body();
                    if (authToken == null) {
                        Log.d(TAG, "refresh token went wrong");
                    } else {
                        if (authToken.getCode() == 201) {
                            Log.i(TAG, "done refresh token");
                            String nt = authToken.getToken();
                            String r = authToken.getRefreshToken();
                            Long exp = authToken.getExpireTime();

                            storeUser(uname, tk, nt, r, exp);

                            Call<UserInfo> getUerInfoCall = userService.getUserInfo(authToken.getToken());
                            getUerInfoCall.enqueue(new Callback<UserInfo>() {

                                @Override
                                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                                    UserInfo userInfo = (UserInfo) response.body();
                                    if (userInfo == null) {
                                        Log.d(TAG, "get user went wrong");
                                    } else {
                                        Log.i(TAG, "done getting user");
//                                        SecuredPreferenceStore loginSharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), OnboardActivity.MODE_PRIVATE);
                                        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
                                        String castgc = loginSharedPref.getString(getString(R.string.pref_login_castgc), "");
                                        userInfo.setCastgc(castgc);
                                        myUser = userInfo;
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserInfo> call, Throwable t) {
                                    Log.d(TAG, "get user fail");
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthToken> call, Throwable t) {
                    Log.d(TAG, "refresh token fail");
                }
            });
        }
    }

    public void updateUser(UserInfo userInfo, boolean noAvatar) {
        loaduser();

        if (noAvatar) {
            userInfo.setAvatar(null);
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))))
                .client(okHttpClient)
                .build();

        UserService userService = retrofit.create(UserService.class);

        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        expire.setTimeInMillis(exp);
        if (now.before(expire)) {
            Call<UserInfo> saveUserInfoCall = userService.saveUserInfo(t, userInfo);
            saveUserInfoCall.enqueue(new Callback<UserInfo>() {
                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    UserInfo userInfo = (UserInfo) response.body();
                    if (userInfo == null) {
                        Log.d(TAG, "update user went wrong");
                    } else {
                        Log.i(TAG, "done updating user");
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {
                    Log.d(TAG, "update user fail");
                }
            });
        } else {
            RequestBody formBody = new FormBody.Builder()
                    .add("username", uname)
                    .add("token", t)
                    .add("refreshToken", rt)
                    .build();

            Retrofit authRetrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.api_url))
                    .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))))
                    .build();

            AuthenticateService authenticateService = authRetrofit.create(AuthenticateService.class);
            Call<AuthToken> authTokenCall = authenticateService.authToken(getString(R.string.origin), formBody);
            authTokenCall.enqueue(new Callback<AuthToken>() {
                @Override
                public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                    AuthToken authToken = (AuthToken) response.body();
                    if (authToken == null) {
                        Log.d(TAG, "refresh token went wrong");
                    } else {
                        if (authToken.getCode() == 201) {
                            Log.i(TAG, "done refresh token");
                            String nt = authToken.getToken();
                            String r = authToken.getRefreshToken();
                            Long exp = authToken.getExpireTime();

                            storeUser(uname, tk, nt, r, exp);

                            Call<UserInfo> saveUserInfoCall = userService.saveUserInfo(authToken.getToken(), userInfo);
                            saveUserInfoCall.enqueue(new Callback<UserInfo>() {
                                @Override
                                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                                    UserInfo userInfo = (UserInfo) response.body();
                                    if (userInfo == null) {
                                        Log.d(TAG, "update user went wrong");
                                    } else {
                                        Log.i(TAG, "done updating user");
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserInfo> call, Throwable t) {
                                    Log.d(TAG, "update user fail");
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthToken> call, Throwable t) {
                    Log.d(TAG, "refresh token fail");
                }
            });
        }
    }

    public void storeUser(String username, String ticket, String t, String r, Long exp) {
        this.uname = username;
        this.tk = ticket;
        this.t = t;
        this.rt = r;
        this.exp = exp;

//        SharedPreferences loginSharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), OnboardActivity.MODE_PRIVATE);
        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
        SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
        prefEditor.putString(getString(R.string.pref_login_username), username);
        prefEditor.putString(getString(R.string.pref_login_ticket), ticket);
        prefEditor.putString(getString(R.string.pref_login_token), t);
        prefEditor.putString(getString(R.string.pref_login_refresh_token), r);
        prefEditor.putLong(getString(R.string.pref_login_exp), exp);
        prefEditor.apply();
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getTk() {
        return tk;
    }

    public void setTk(String tk) {
        this.tk = tk;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public static void setMyUser(UserInfo user) {
        OnboardActivity.myUser = user;
    }

    public static UserInfo getMyUser() {
        return myUser;
    }
}
