/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk;

import static com.google.android.gms.fitness.data.Field.FIELD_CALORIES;
import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static com.taiwanlife.teamwalk.util.Utilities.randomString;
import static com.taiwanlife.teamwalk.util.Utilities.sha1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.HistoryClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taiwanlife.teamwalk.login.LoginActivity;
import com.taiwanlife.teamwalk.model.FitCalories;
import com.taiwanlife.teamwalk.model.FitStep;
import com.taiwanlife.teamwalk.model.FitbitToken;
import com.taiwanlife.teamwalk.model.request.LoginRequest;
import com.taiwanlife.teamwalk.model.response.LoginResponse;
import com.taiwanlife.teamwalk.onboard.AvatarActivity;
import com.taiwanlife.teamwalk.service.FitbitTokenService;
import com.taiwanlife.teamwalk.service.GarminTokenService;
import com.taiwanlife.teamwalk.service.LoginService;
import com.taiwanlife.teamwalk.service.UserService;
import com.taiwanlife.teamwalk.share.ShareUtil;
import com.taiwanlife.teamwalk.util.CelebrusCSAUtil;
import com.taiwanlife.teamwalk.util.DeviceUtil;
import com.taiwanlife.teamwalk.util.FortifyUtil;
import com.taiwanlife.teamwalk.util.PermissionUtil;
import com.taiwanlife.teamwalk.util.SensitiveDataUtil;
import com.taiwanlife.teamwalk.util.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@SuppressLint("HardwareIds")
public class MainActivity extends AppCompatActivity implements ProviderInstaller.ProviderInstallListener {

    private static final String TAG = "MainActivity";
    private static final String APP = "Teamwalk";

    private static final int MAIN_URL_RES = R.string.web_url;
    public static final int LOGIN_REQUEST = 1;
    public static final int GOOGLE_SIGN_IN = 2;
    public static final int PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 3;
    public static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 4;
    public static final int NICKNAME_REQUEST_SELECT_IMAGE = 5;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6;
    public static final int CREATE_TEAM_REQUEST_SELECT_IMAGE = 7;
    public static final int GOOGLE_SIGN_IN_FOR_DISABLE_FIT = 8;
    public static final String ON_BOARD_FINISH = "ON_BOARD_FINISH";

    private Intent uploadIntent;
    private GoogleSignInClient googleSignInClient;
    private String authCode;
    private String fitbit_base_url = "https://api.fitbit.com/oauth2/";
    private String garmin_base_url = "https://connectapi.garmin.com/oauth-service/oauth/";

    private String fcmToken;
    private String fid;
    private SecuredPreferenceStore loginSharedPref;
    private WebView webView;

    private String pid;
    private String ticket;
    private String teamwalkToken;

    private ValueCallback<Uri[]> ulmsg;
    private ProgressBar loadingIndicator;
    private TextView loadingText;

    // step和calories可能手动输入数据，因此按照小时获取，并手动合并
//    private List<Map<String, Object>> stepDataList = new ArrayList<>();
//    private List<Map<String, Object>> caloriesDataList = new ArrayList<>();
    private List<Map<String, Long>> sleepDataList = new ArrayList<>();
    private Map<String, FitStep> stepDataMap = new HashMap<>();
    private Map<String, FitCalories> caloriesDataMap = new HashMap<>();

    // webconnect garmin can not have parameter， local
    private String tsGarmin = "";
    private boolean clearCache = false;

    boolean isKnowsDeviceSecure = false;
    boolean isKnowsReverseToolRunning = false;
    boolean isKnowsCovered = false;

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void logSentFriendRequestEvent () {
        Log.i(TAG, "fb: logSentFriendRequestEvent");
        AppEventsLogger.newLogger(this).logEvent("sentFriendRequest");
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        DeviceUtil.setFlagSecure(this);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(this.getColor(android.R.color.transparent));
        // create notification channel
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(getString(R.string.noti_channel_id), "default", importance);
        channel.setDescription("default channel for system wide push notifications");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // register fcm
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fail to register FCM token",  task.getException());
                        return;
                    }

                    fcmToken = task.getResult();
                });

        // shared preferences
//        loginSharedPref = getSharedPreferences(getString(R.string.pref_login), MainActivity.MODE_PRIVATE);
        loginSharedPref = SecuredPreferenceStore.getSharedInstance();
        pid = loginSharedPref.getString(getString(R.string.pref_login_pid), "");
        ticket = loginSharedPref.getString(getString(R.string.pref_login_ticket), "");

        SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
//        prefEditor.clear();

        // get fid
        try{
            FirebaseInstallations.getInstance().getId()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Unable to get FID");

                            }

                            fid = task.getResult();
//                        Log.d(TAG, "fid: " + fid);

                            prefEditor.putString(getString(R.string.pref_login_fid), fid);
                            prefEditor.apply();
                        }
                    });

        }catch (Exception e){}

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingText = findViewById(R.id.loading_text);
        CelebrusCSAUtil.start(this);
        setMainWebView();
        CelebrusCSAUtil.sessionSharing(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PermissionUtil.checkPermission(MainActivity.this);
            }
        },10);
//        checkPermission();
//        verifyStoragePermissions(this);
//        if(shareUri!=null)
//            ShareUtil.deleteUri(MainActivity.this, shareUri);
//        shareUri=null;
        ShareUtil.delShareImage(MainActivity.this);

        initGooglePlay();

        ProviderInstaller.installIfNeededAsync(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        CelebrusCSAUtil.start(this);
        restoreSensitiveData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 驗證App是否遭到破壞，只針對UAT
//        if (!validateSign()) {
//            illegalApp();
//            return;
//        }
        boolean knowsRoot = loginSharedPref.getBoolean(getString(R.string.knows_root), false);
        if (DeviceUtil.isDeviceRooted() && !knowsRoot) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("提醒您，若在非一般環境(Root/Jailbreak/刷機)使用Teamwalk APP可能會有資訊外流的風險");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

            //设置正面按钮
            builder.setPositiveButton("我知道了並繼續使用", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doBusiness();
                    dialog.dismiss();
                    SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
                    prefEditor.putBoolean(getString(R.string.knows_root), true);
                    prefEditor.apply();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
//        else if(!DeviceUtil.isDeviceSecure(this) && !isKnowsDeviceSecure){
//            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setMessage("手機未設定密碼，有資安疑慮請設定密碼，謝謝");
//            builder.setIcon(R.mipmap.ic_launcher);
//            builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失
//
//            //设置正面按钮
//            builder.setPositiveButton("我知道了並繼續使用", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    doBusiness();
//                    dialog.dismiss();
//                    isKnowsDeviceSecure = true;
//                }
//            });
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }
        else if(DeviceUtil.isReverseToolRunning(this) && !isKnowsReverseToolRunning){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("提醒您，有逆向工具運行，APP可能會有資訊外流的風險");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

            //设置正面按钮
            builder.setPositiveButton("我知道了並繼續使用", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doBusiness();
                    dialog.dismiss();
                    isKnowsReverseToolRunning = true;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(DeviceUtil.isCovered(this)){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("提醒您，屏幕被覆蓋，APP可能會有資訊外流的風險");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

            //设置正面按钮
            builder.setPositiveButton("我知道了並繼續使用", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doBusiness();
                    dialog.dismiss();
                    isKnowsCovered = true;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            doBusiness();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        CelebrusCSAUtil.stop();
        if (clearCache) {
            webView.clearCache(true);
        }
        backupSensitiveData();
        clearSensitiveData(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
            String scheme_action = uri.getHost();

            switch (scheme_action) {
                case "login":
                    toLogin();
                    break;
                case "loginsuccess":
                    // 1.傳輸手機資訊來獲取 JWT
                    // 取得 Ticket
                    ticket = uri.getQueryParameter("ticket");
                    // 取得 UUID
                    String uuid = loginSharedPref.getString(getString(R.string.pref_login_uuid), "");
                    // 取得 DeviceId
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                    LoginRequest request = new LoginRequest(
                            ticket,
                            "",
                            uuid,
                            deviceId,
                            fcmToken
                    );
                    Log.e("GGG", request.toString());

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://demo.mutron.com.tw/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    LoginService loginService = retrofit.create(LoginService.class);
                    loginService.login(request).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String jwt = response.body().getData().getToken();
                                Log.e("GGG", jwt);

                                // 儲存到 SharedPreferences
                                // prefEditor.putBoolean(getString(R.string.pref_login_auth), true);
                                prefEditor.putString(getString(R.string.pref_login_username), pid);
                                prefEditor.putString(getString(R.string.pref_login_ticket), ticket);
                                prefEditor.putString(getString(R.string.pref_login_jwt_token), jwt);
                                prefEditor.apply();

                                // String token = prefs.getString("jwt_token", null);

                            } else {
                                Log.e("GGG", "登入失敗");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                            Toast.makeText(MainActivity.this, "處理失敗", Toast.LENGTH_LONG).show();
                        }
                    });

                    webView.loadUrl(getString(MAIN_URL_RES));
                    break;
                case "home":
                    webView.loadUrl(getString(MAIN_URL_RES));
                    break;

                case "userinfo":
//                    if(getPwPageFlag())
//                        webView.loadUrl(getString(MAIN_URL_RES));
//                    else
//                        webView.loadUrl(getString(MAIN_URL_RES) + "my/preferences");
//                    setPwPageFlag(false);
                    break;
                case "onboarding":
                    Intent onboardingIntent = new Intent(this, AvatarActivity.class);
                    onboardingIntent.setData(intent.getData());
                    onboardingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(onboardingIntent);
                    break;
                case "webconnect":
                    String deviceRaw = uri.getQueryParameter("device");
                    String device = deviceRaw.indexOf("@") > 0 ? deviceRaw.substring(0, deviceRaw.indexOf("@")) : deviceRaw;

                    if (TextUtils.equals(device, "fitbit")) {
                        Toast connFailToast = Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG);

                        String encodeAuthString = getString(R.string.connect_fitbit_client_id) + ":" + getString(R.string.connect_fitbit_client_secret);
                        String authorizationValue = "Basic " + Base64.encodeToString(encodeAuthString.getBytes(), Base64.NO_WRAP);
                        String code = uri.getQueryParameter("code");
                        String error = uri.getQueryParameter("error");
                        /**
                         * success: teamwalkuat://webconnect?device=fitbit&code=22720838ec5350eacf40bd07dbcbe2295d0e8e79#_=_
                         * error: teamwalkuat://webconnect?device=fitbit&error_description=The+user+denied+the+request.&error=access_denied#_=_
                         */
                        if (code == null && error != null) {
                            webView.reload();
                            connFailToast.show();
                            return;
                        }
                        Gson gson = new GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create();

                        Retrofit retrofitFitbit = new Retrofit.Builder()
                                .baseUrl(fitbit_base_url)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .build();

                        FitbitTokenService fitbitTokenService = retrofitFitbit.create(FitbitTokenService.class);
                        Call<FitbitToken> getTokenCall = fitbitTokenService.getTocken(authorizationValue,
                                uri.getQueryParameter("code"),
                                "authorization_code",
                                getString(R.string.connect_fitbit_client_id),
                                "teamwalk" + getString(R.string.env) + "://webconnect?device=fitbit");

                        getTokenCall.enqueue(new Callback<FitbitToken>() {

                            // {
                            //   "access_token": "",
                            //   "expires_in": 28800,
                            //   "refresh_token": "",
                            //   "scope": "sleep activity",
                            //   "token_type": "Bearer",
                            //   "user_id": ""
                            // }
                            @Override
                            public void onResponse(Call<FitbitToken> call, Response<FitbitToken> response) {
                                FitbitToken fitbitToken = response.body();
//                        Log.d(TAG, "fitbit get token " + fitbitToken.getAccessToken());
//                        Log.d(TAG, "fitbit expires " + fitbitToken.getExpiresIn());
//                        Log.d(TAG, "fitbit refresh " + fitbitToken.getRefreshToken());
//                        Log.d(TAG, "fitbit scope " + fitbitToken.getScope());
//                        Log.d(TAG, "fitbit type " + fitbitToken.getTokenType());
//                        Log.d(TAG, "fitbit user " + fitbitToken.getUserId());

                                if (fitbitToken == null) {
                                    connFailToast.show();
                                } else {
                                    webView.loadUrl(getString(MAIN_URL_RES) + "health/connect?device=fitbit&t=" + fitbitToken.getAccessToken() + "&r=" + fitbitToken.getRefreshToken());
                                    Toast.makeText(MainActivity.this, getString(R.string.onboard_connect_success), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<FitbitToken> call, Throwable t) {
                                connFailToast.show();
                            }
                        });
                    }
                    break;
                case "webconnectgarmin":
//                    String deviceGarminRaw = uri.getQueryParameter("device");
                    Log.d(TAG, "connect garmin");
//                        Log.d(TAG, uri.getQueryParameter("oauth_token"));
//                        Log.d(TAG, uri.getQueryParameter("oauth_verifier"));
//                    String oats = deviceGarminRaw.substring(deviceGarminRaw.indexOf("@") + 1, deviceGarminRaw.length());
                    String oats = tsGarmin;
//                        Log.d(TAG, oats);

                    Toast connFailToast = Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG);

                    String oauthConsumerKey = getString(R.string.connect_garmin_consumer_key);
                    String oauthToken = uri.getQueryParameter("oauth_token");
                    String oauthSignatureMethod = "HMAC-SHA1";

                    String oauthNonce = randomString();

                    Date today = new Date();
                    long timestamp = today.getTime() / 1000L;
                    String oauthTimestamp = String.valueOf(timestamp);
                    String oauthVersion = "1.0";
                    String oauthVerifier = uri.getQueryParameter("oauth_verifier");
                    /**
                     * success: teamwalkuat://webconnectgarmin?oauth_token=2c60725c-c48d-4d92-84b5-01a3a334659c&oauth_verifier=nz9Fo2HKpo
                     * error: teamwalkuat://webconnectgarmin?oauth_token=63590214-f100-471f-a49f-0a8f1177362e&oauth_verifier=null
                     */
                    if (oauthVerifier == null || oauthVerifier.equals("null")) {
                        webView.reload();
                        connFailToast.show();
                        return;
                    }
                    String signature = "oauth_consumer_key=" + oauthConsumerKey + "&" +
                            "oauth_nonce=" + oauthNonce + "&" +
                            "oauth_signature_method=" + oauthSignatureMethod + "&" +
                            "oauth_timestamp=" + oauthTimestamp + "&" +
                            "oauth_token=" + oauthToken + "&" +
                            "oauth_verifier=" + oauthVerifier + "&" +
                            "oauth_version=" + oauthVersion;
                    String signatureBaseString = "";
                    try {
                        String signatureBase = URLEncoder.encode(garmin_base_url + "access_token", "utf-8") + "&" + URLEncoder.encode(signature, "utf-8");
                        signatureBaseString = "POST&" + signatureBase;
                    } catch (UnsupportedEncodingException e) {
//                            Log.e(TAG, "Fail to encode signature string", e);
                    }

                    String keyString = getString(R.string.connect_garmin_comsumer_secret) + "&" + oats;
                    String oauthSignature = "";
                    try {
                        oauthSignature =  URLEncoder.encode(sha1(signatureBaseString, keyString), "utf-8");
                    } catch (UnsupportedEncodingException e) {
//                            Log.e(TAG, "Fail to encode oauth signature string", e);
                    } catch ( NoSuchAlgorithmException  e) {
//                            Log.e(TAG, "Fail to encode oauth signature string", e);
                    } catch ( InvalidKeyException e) {
//                            Log.e(TAG, "Fail to encode oauth signature string", e);
                    }

                    String authorization = "OAuth " + "oauth_verifier=\"" + oauthVerifier + "\", " +
                            "oauth_version=\"" + oauthVersion + "\", " +
                            "oauth_consumer_key=\"" + oauthConsumerKey + "\", " +
                            "oauth_token=\"" + oauthToken + "\", " +
                            "oauth_timestamp=\"" + oauthTimestamp + "\", " +
                            "oauth_nonce=\"" + oauthNonce + "\", " +
                            "oauth_signature_method=\"" + oauthSignatureMethod + "\", " +
                            "oauth_signature=\"" + oauthSignature + "\"";

                    Retrofit retrofitGarmin = new Retrofit.Builder()
                            .baseUrl(garmin_base_url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    GarminTokenService garminTokenService = retrofitGarmin.create(GarminTokenService.class);
                    Call<ResponseBody> getTokenCall = garminTokenService.getToken(authorization);

                    getTokenCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                ResponseBody tokenString = response.body();
//                                String token = "";
//                                try {
//                                    token = tokenString.string();
//                                } catch (IOException e) {
//                                    connFailToast.show();
//                                }
//
//                                if (TextUtils.isEmpty(token)) {
//                                    connFailToast.show();
//                                } else {
//                                    String oauthToken = token.substring(token.indexOf("=") + 1, token.indexOf("&"));
//                                    String oauthTokenSecret = token.substring(token.lastIndexOf("=") + 1, token.length());
//
//                                    webView.loadUrl(getString(MAIN_URL_RES) + "health/connect?device=garmin&t=" + oauthToken + "&s=" + oauthTokenSecret);
//                                    Toast.makeText(MainActivity.this, getString(R.string.onboard_connect_success), Toast.LENGTH_LONG).show();
//                                }

                            try{
                                if(response.body()!=null){
                                    String responseString = response.body().string();
                                    if (TextUtils.isEmpty(responseString))
                                        connFailToast.show();
                                    else {
                                        String oauthToken = responseString.substring(responseString.indexOf("=") + 1, responseString.indexOf("&"));
                                        String oauthTokenSecret = responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length());

                                        webView.loadUrl(getString(MAIN_URL_RES) + "health/connect?device=garmin&t=" + oauthToken + "&s=" + oauthTokenSecret);
                                        Toast.makeText(MainActivity.this, getString(R.string.onboard_connect_success), Toast.LENGTH_LONG).show();
                                    }
                                }else
                                    connFailToast.show();
                            }catch (Exception e){
                                connFailToast.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            connFailToast.show();
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    accessGoogleFit();
                } else {
                    Log.d(TAG, "permission not Granted");
                    // TODO implement dialog of the result of permission not granted
                }
                return;

//            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startActivityForResult(uploadIntent, MainActivity.NICKNAME_REQUEST_SELECT_IMAGE);
//                } else {
//                    Log.d(TAG, "permission not Granted");
//                    // TODO implement dialog of the result of permission not granted
//                }
//                return;
//            case PERMISSIONS_EXTERNAL_STORAGE_RECOGNITION:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(MainActivity.this, "儲存權限申請成功!", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "儲存權限申請失敗!", Toast.LENGTH_LONG).show();
//                }
//                break;
            case PermissionUtil.permissionRequestCode:
                PermissionUtil.onCheckPermission(permissions, grantResults, this);
                break;
            case CAMERA_PERMISSION_REQUEST_CODE:
                // 確認相機權限是否獲准
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 已獲得權限，啟動相機
                    launchCamera();
                } else {
                    // 權限被拒絕，通知 WebView 無檔案，並提示用戶
                    if (filePathCallback != null) {
                        filePathCallback.onReceiveValue(null);
                        filePathCallback = null;
                    }
                    Toast.makeText(this, "相機權限被拒絕，無法使用拍照功能", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return;
        }
    }
    // 4. 选择内容回调到Html页面
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
//        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) {
//            return;
//        }
//        Uri[] results = null;
//        if (resultCode == Activity.RESULT_OK) {
//            if (intent != null) {
//                String dataString = intent.getDataString();
//                ClipData clipData = intent.getClipData();
//                if (clipData != null) {
//                    results = new Uri[clipData.getItemCount()];
//                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                        ClipData.Item item = clipData.getItemAt(i);
//                        results[i] = item.getUri();
//                    }
//                }
//                if (dataString != null) {
//                    results = new Uri[]{Uri.parse(dataString)};
//                }
//            }
//        }
//        uploadMessageAboveL.onReceiveValue(results);
//        uploadMessageAboveL = null;
//    }
//    private Uri shareUri;
//    public void setShareContentUri(Uri uri){
//        if(shareUri!=null)
//            ShareUtil.deleteUri(MainActivity.this, shareUri);
//        shareUri=null;
//        shareUri = uri;
//
//    }

    @Override
    protected void onDestroy() {
//        if(shareUri!=null)
//            ShareUtil.deleteUri(MainActivity.this, shareUri);
//        shareUri=null;
//        ShareUtil.delShareImage(MainActivity.this);
        clearSensitiveData(true);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== ShareUtil.SHARECONTENT_REQUEST){
//            Log.i(TAG,"Share Content return:" + data);
//            if(shareUri!=null)
//                ShareUtil.deleteUri(MainActivity.this, shareUri);
//            shareUri=null;
//            ShareUtil.delShareImage(MainActivity.this);
        }
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            retryProviderInstall = true;
        }

//        if (requestCode == FILE_CHOOSER_RESULT_CODE){
//            if (null == uploadMessage && null == uploadMessageAboveL) {
//                return;
//            }
//            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
//            // Uri result = (((data == null) || (resultCode != RESULT_OK)) ? null : data.getData());
//            if (uploadMessageAboveL != null) {
//                onActivityResultAboveL(requestCode, resultCode, data);
//            } else if (uploadMessage != null) {
//                uploadMessage.onReceiveValue(result);
//                uploadMessage = null;
//            }else uploadMessage = null;
//        }
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            Uri[] results = null;
            // 確認操作成功
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    // 取得使用者選擇的圖片或檔案之 Uri
                    Uri selectedFileUri = data.getData();
                    results = new Uri[]{selectedFileUri};
                } else if (cameraImageUri != null) {
                    // 若 data 為 null，可能是相機拍照的結果，使用先前保存的照片 Uri
                    results = new Uri[]{cameraImageUri};
                }
            }
            // 將結果傳回給 WebView 的檔案上傳回調
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        } else
        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == FILE_CHOOSER_RESULT_CODE) {
//                if (null == uploadMessage && null == uploadMessageAboveL) {
//                    return;
//                }
//                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
//                // Uri result = (((data == null) || (resultCode != RESULT_OK)) ? null : data.getData());
//                if (uploadMessageAboveL != null) {
//                    onActivityResultAboveL(requestCode, resultCode, data);
//                } else if (uploadMessage != null) {
//                    uploadMessage.onReceiveValue(result);
//                    uploadMessage = null;
//                }
//            }
            if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
                Uri[] results = null;
                // 確認操作成功
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        // 取得使用者選擇的圖片或檔案之 Uri
                        Uri selectedFileUri = data.getData();
                        results = new Uri[]{selectedFileUri};
                    } else if (cameraImageUri != null) {
                        // 若 data 為 null，可能是相機拍照的結果，使用先前保存的照片 Uri
                        results = new Uri[]{cameraImageUri};
                    }
                }
                // 將結果傳回給 WebView 的檔案上傳回調
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
            }
            if (requestCode == LOGIN_REQUEST) {
                pid = data.getStringExtra("pid");
                String url = data.getStringExtra("url");
                byte[] postData = data.getStringExtra("params").getBytes();
                if(URLUtil.isNetworkUrl(url)){
                    webView.postUrl(url, postData);
                }
            }

            if (requestCode == GOOGLE_SIGN_IN) {
                FitnessOptions fitnessOptions = FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                        .build();

                GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

                if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                    GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account, fitnessOptions);
                } else {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount googleAccount = task.getResult(ApiException.class);
                        authCode = googleAccount.getServerAuthCode();
                    } catch (ApiException e) {
                        Log.d(TAG, "Fail to connect google fit", e);
                        Log.w(TAG, "Fail to connect google fit");
                        Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG).show();
                    }

                    if (TextUtils.isEmpty(authCode)) {
                        Log.i(TAG, "Fail to connect google fit, no auth code");
                        Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG).show();
                    } else {
                        webView.loadUrl(getString(MAIN_URL_RES) + "health/connect?device=google&a=" + authCode);
                        Toast.makeText(this, getString(R.string.onboard_connect_success), Toast.LENGTH_LONG).show();
                    }
                }
            }

            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    authCode = account.getServerAuthCode();
                } catch (ApiException e) {
                    Log.i(TAG, "Fail to connect google fit", e);
                    Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG).show();
                }

                if (TextUtils.isEmpty(authCode)) {
                    Log.i(TAG, "Fail to connect google fit, no auth code");
                    Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG).show();
                } else {
                    webView.loadUrl(getString(MAIN_URL_RES) + "health/connect?device=google&a=" + authCode);
                    Toast.makeText(this, getString(R.string.onboard_connect_success), Toast.LENGTH_LONG).show();
                }
            }

            if (requestCode == GOOGLE_SIGN_IN_FOR_DISABLE_FIT) {
                FitnessOptions fitnessOptions = FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                        .build();

                Fitness.getConfigClient(this,  GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                        .disableFit()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG,"Disabled Google Fit");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG,"Fail to disabling Google Fit");
                            }
                        });
            }

            if (requestCode == NICKNAME_REQUEST_SELECT_IMAGE) {
                if (ulmsg == null) {
                    return;
                }
                ulmsg.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                ulmsg = null;
            }

            if (requestCode == CREATE_TEAM_REQUEST_SELECT_IMAGE) {
                if (ulmsg == null) {
                    return;
                }
                ulmsg.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                ulmsg = null;
            }
        }
    }

    private void doBusiness() {
        Uri uri = getIntent().getData();

        if (uri != null) {
            String scheme_action = uri.getHost();

            getIntent().setData(null);
//            if (webView.getUrl().endsWith("my/preferences")) {
//                webView.loadUrl(getString(MAIN_URL_RES) + "my/preferences");
//            }
        } else {
            if (webView.getUrl() != null) {
                String url = webView.getUrl();

                if (webView.getUrl().endsWith("logout")) {
                    toLogin();
                }

                if (webView.getUrl().endsWith("blank")) {
                    Log.i(TAG, "logout by blank");
                    toLogin();
                }

                if (getIntent().getAction() != null && getIntent().getAction().equals(ON_BOARD_FINISH)) {
                    webView.loadUrl(getString(MAIN_URL_RES) + "main");
                }
                if (webView.getUrl().endsWith("my/preferences")) {
                    webView.loadUrl(getString(MAIN_URL_RES) + "my/preferences");
                }
            } else {
                boolean isAuthenticated = loginSharedPref.getBoolean(getString(R.string.pref_login_auth), false);

                if (isAuthenticated) {
                    webView.loadUrl(getString(MAIN_URL_RES));
                } else {
                    toLogin();
                }
            }
        }

        if (!isOnline()) {
            Toast.makeText(MainActivity.this, getString(R.string.main_is_not_online), Toast.LENGTH_LONG).show();
        }

    }

    private void toLogin() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
        webView.clearCache(true);
        webView.loadUrl("about:blank");

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, LOGIN_REQUEST);
    }

    private void backupSensitiveData(){
        SecuredPreferenceStore pref = SecuredPreferenceStore.getSharedInstance();
        SecuredPreferenceStore.Editor editor = pref.edit();
        editor.putString(getString((R.string.pref_login_pid)), pid);
        editor.putString(getString(R.string.pref_login_ticket), ticket);
    }

    private void restoreSensitiveData(){
        SecuredPreferenceStore pref = SecuredPreferenceStore.getSharedInstance();
        pid = pref.getString(getString(R.string.pref_login_pid), "");
        ticket = pref.getString(getString(R.string.pref_login_ticket), "");
    }

    private void clearSensitiveData(boolean isDestroy) {
        if(isDestroy) {
            webView.loadUrl("about:blank");
            SensitiveDataUtil.clearWebViewSensitiveData(this, webView, isDestroy);
        }
        if (pid != null) {
            Arrays.fill(pid.toCharArray(), '\0');
            pid = null;
        }
        if (ticket != null) {
            Arrays.fill(ticket.toCharArray(), '\0');
            ticket = null;
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void accessGoogleFit() {
        String serverClientId = getString(R.string.connect_google_client_id);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/fitness.activity.read"), new Scope("https://www.googleapis.com/auth/fitness.sleep.read"))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            googleSignInClient.signOut();
        }
        startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN);
    }

    private void setMainWebView() {
//        webView = new WebView(this);
//        setContentView(webView);
        webView = findViewById(R.id.main_webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "/env=taiwanlife_teamwalk_app");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        webSettings.setBuiltInZoomControls(false);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            webSettings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        // if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //     WebView.setWebContentsDebuggingEnabled(true);
        // }

        WebView.setWebContentsDebuggingEnabled(false);


        webView.setWebViewClient(new MainWebViewClient(this));
        webView.setWebChromeClient(new MainWebChromeClient(this));
        webView.addJavascriptInterface(new WebAppInterface(), "android");
        com.speed_trap.android.WebAppInterface webAppInterface = new com.speed_trap.android.WebAppInterface(webView);
        webView.addJavascriptInterface(webAppInterface, webAppInterface.getAppBridgeJsName());

//        CelebrusCSAUtil.sessionSharing(this);
    }

    @Override
    public void onProviderInstalled() {

    }

    private static final int ERROR_DIALOG_REQUEST_CODE = 1111;
    private boolean retryProviderInstall;
    @Override
    public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        if (availability.isUserResolvableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
//            availability.showErrorDialogFragment(
//                    this,
//                    errorCode,
//                    ERROR_DIALOG_REQUEST_CODE,
//                    new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            // The user chose not to take the recovery action
//                            onProviderInstallerNotAvailable();
//                        }
//                    });
            onProviderInstallerNotAvailable();
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }
    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        Toast.makeText(MainActivity.this, "Google Play服務不可用。", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (retryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        retryProviderInstall = false;
    }

    private class MainWebViewClient extends WebViewClient {

        private Context context;

        public MainWebViewClient(Context context) {
            this.context = context;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().equals(getString(R.string.web_url)) && !view.getTitle().equals("Teamwalk")) {
                webView.clearCache(true);
            }
            if (request.getUrl().toString().startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            } else {

                Uri cssoURL = Uri.parse(getString(R.string.csso_url));
                if (TextUtils.equals(cssoURL.getHost(), request.getUrl().getHost())) {
                    if (TextUtils.equals("/csso/mobileIndex", request.getUrl().getPath())) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://login"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    }

                    return false;
                }

                Uri tcavURL = Uri.parse(getString(R.string.tcav_url));
                if (TextUtils.equals(tcavURL.getHost(), request.getUrl().getHost())) {
                    return false;
                }

                Uri webURL = Uri.parse(getString(R.string.web_url));
                if (TextUtils.equals(webURL.getHost(), request.getUrl().getHost())) {
                    return false;
                }

                Uri taiwanlife_member_uri =  Uri.parse(getString(R.string.taiwanlife_member_url));
                if (TextUtils.equals(taiwanlife_member_uri.getHost(), request.getUrl().getHost())) {
                    String taiwanlife_member_str =  getString(R.string.taiwanlife_member_url);
                    String request_str=  request.getUrl().toString();
                    try {
                        taiwanlife_member_str =  URLDecoder.decode(getString(R.string.taiwanlife_member_url));
                        request_str=  URLDecoder.decode(request.getUrl().toString());
                    }catch (Exception e){
                        taiwanlife_member_str =  getString(R.string.taiwanlife_member_url);
                        request_str=  request.getUrl().toString();
                    }
                    if (TextUtils.equals(taiwanlife_member_str, request_str)) {
                        webView.loadUrl(getString(MAIN_URL_RES) + "my/preferences");
                        Log.i(TAG, "shouldOverrideUrlLoading: return my/preferences for www.taiwanlife.com");
                        showPwErrorDialog();
                        return true;
                    }
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//            Log.i("LOG TIME webview url: " + url , Utilities.getDateNow());
            loadingIndicator.setVisibility(View.VISIBLE);
            if (url.startsWith(getString(R.string.csso_url))) {
                loadingText.setVisibility(View.VISIBLE);
                loadingText.setTextColor(Color.BLACK);
            } else if (url.startsWith("teamwalk")) {
                Uri parse = Uri.parse(url);
                String host = parse.getHost();
                String actionUrl = "";
                String suffic = "teamwalk" + getString(R.string.env);
                switch (host) {
                    case "home":
                        actionUrl = suffic + "://home";
                        break;
                    case "login":
                        actionUrl = suffic + "://login";
                        break;
                    case "loginsuccess":
                        actionUrl = suffic + "://loginsuccess?ticket=" + parse.getQueryParameter("ticket");
                        break;
                    case "loginfailure":
                        actionUrl = suffic + "://loginfailure";
                        break;
                    case "userinfo":
                        actionUrl = suffic + "://userinfo";
                        break;
                    case "webconnect":
                        actionUrl = suffic + "://webconnect";
                        break;
                }

                if (!actionUrl.equals("")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl));
                    intent.putExtra("pid", pid);
                    view.stopLoading();
                    startActivity(intent);
                    if (url.contains("loginsuccess")) {
                        webView.loadUrl("about:blank");
                    }
                } else {
                    toLogin();
                }
            } else if (url.equals(getString(R.string.web_url))) {
                loadingText.setVisibility(View.VISIBLE);
                loadingText.setTextColor(Color.GRAY);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (view.getUrl().equals(getString(R.string.web_url)) && !view.getTitle().equals("Teamwalk")) {
                clearCache = true;
            }
            String[] buildInfo = FortifyUtil.getBuildInfo(context);
            String csso_url = buildInfo[0];     // getString(R.string.csso_url);
            String web_url = buildInfo[1];     //getString(R.string.web_url);

            if (csso_url.length() > 0 && web_url.length() > 0) {
                String cookies = CookieManager.getInstance().getCookie(csso_url + "login");
                if (!TextUtils.isEmpty(cookies)) {
                    String[] cookieStringArray = cookies.split(";");
                    String CASTGC_value = null;
                    for (String cookie : cookieStringArray) {
                        if (cookie.contains("CASTGC")) {
                            CASTGC_value = "";
                            String[] cookieArray = cookie.split("=");
                            if(cookieArray.length<2)
                                continue;
                            String cookieValue = cookieArray[1];
                            CASTGC_value = cookieValue;
                            SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
                            prefEditor.putString(getString(R.string.pref_login_castgc), cookieValue);
                            prefEditor.apply();
                            break;
                        }
                    }

                    //Log.i("Cookies:", "onPageFinished setCookie:url="+url);
                    String loginsuccessStr =  "teamwalkuat://loginsuccess";
                    if(BuildConfig.BUILD_TYPE.compareToIgnoreCase("release")==0)
                        loginsuccessStr =  "teamwalk://loginsuccess";
                    if(url.indexOf(loginsuccessStr)==0 && CASTGC_value!=null){
                        String newCookies = "CASTGC="+ CASTGC_value;
                        CookieManager.getInstance().setCookie(web_url, newCookies);
                        CookieManager.getInstance().flush();
                        //Log.i("Cookies:", "onPageFinished setCookie:cookies=" + cookies);
                        //Log.i("Cookies:", "onPageFinished loginsuccess setCookie:newCookies=" + newCookies);
                    }else
                    {
                        String newCookies = FortifyUtil.filterCookiesForFortify(cookies);
                        if (newCookies.length() > 0) {
                            CookieManager.getInstance().setCookie(web_url, newCookies);
                            CookieManager.getInstance().flush();
                            //Log.i("Cookies:", "onPageFinished setCookie:cookies=" + cookies);
                            //Log.i("Cookies:", "onPageFinished setCookie:newCookies=" + newCookies);
                        }
                    }

                    //                CookieManager.getInstance().setCookie(getString(R.string.web_url), cookies);
                    //                CookieManager.getInstance().flush();
                }
            }

            loadingIndicator.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        }

//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            super.onReceivedSslError(view, handler, error);
//            if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("sit")) {
//                handler.proceed();
//            }
//        }
    }

//    private ValueCallback<Uri> uploadMessage;
//    private ValueCallback<Uri[]> uploadMessageAboveL;
//    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private ValueCallback<Uri[]> filePathCallback;  // 保存 WebView 檔案上傳的回調
    private Uri cameraImageUri;                    // 保存相機拍照得到的照片 Uri
    private static final int FILE_CHOOSER_REQUEST_CODE = 100;   // 檔案選擇請求代碼
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101; // 相機權限請求代碼
    private class MainWebChromeClient extends WebChromeClient {

        private MainActivity context;

        public MainWebChromeClient(MainActivity context) {
            this.context = context;
        }

//        // For Android < 3.0
//        public void openFileChooser(ValueCallback<Uri> valueCallback) {
//            uploadMessage = valueCallback;
//            openImageChooserActivity();
//        }
//
//        // For Android  >= 3.0
//        public void openFileChooser(ValueCallback valueCallback, String acceptType) {
//            uploadMessage = valueCallback;
//            openImageChooserActivity();
//        }
//
//        //For Android  >= 4.1
//        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
//            uploadMessage = valueCallback;
//            openImageChooserActivity();
//        }

        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallbackParam, WebChromeClient.FileChooserParams fileChooserParams) {
//            uploadMessageAboveL = filePathCallback;
//            openImageChooserActivity();
//            return true;
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
            }
            filePathCallback = filePathCallbackParam;

            // 準備選項清單
            String[] selectOptions = new String[]{"選擇圖片", "拍照", "檔案"};
            showBottomSheetDialog(); // 呼叫下方自訂的方法
            return true;  // 已自行處理檔案選擇介面
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            result.confirm();
                        }
                    })
                    .setCancelable(false)
                    .create();
            alertDialog.show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            result.confirm();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            result.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create();
            alertDialog.show();
            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//            Log.i(TAG +"  console" , "["+consoleMessage.messageLevel()+"] "+ consoleMessage.message());
            return true;
        }
    }
    // 2.回调方法触发本地选择文件
//    private void openImageChooserActivity() {
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");//图片上传
//        //        i.setType("file/*");//文件上传
////        i.setType("*/*");//文件上传
//        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
//    }

    private class WebAppInterface {

        @JavascriptInterface
        public String signInUser() {
            String token = loginSharedPref.getString(getString(R.string.pref_login_token), "");
            String refreshToken = loginSharedPref.getString(getString(R.string.pref_login_refresh_token), "");
            Long exp = loginSharedPref.getLong(getString(R.string.pref_login_exp), 0L);
            String castgc = loginSharedPref.getString(getString(R.string.pref_login_castgc), "");
            String uUid = loginSharedPref.getString(getString(R.string.pref_login_uuid), "");
            if (uUid == null || uUid.equals("")) {
                uUid = fid;
                loginSharedPref.edit().putString(getString(R.string.pref_login_uuid), uUid);
            }
            Map<String, String> user = new HashMap<String, String>();
            user.put("pid", pid);
            user.put("ticket", ticket);
            user.put("token", token);
            user.put("refreshToken", refreshToken);
            user.put("exp", String.valueOf(exp));
            user.put("castgc", castgc);
            user.put("fcm", fcmToken);
            user.put("fid", uUid);
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                user.put("vNo", pInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Fail to get package version");
            }
            return (new Gson()).toJson(user);
        }

        @JavascriptInterface
        public void copyToClipboard(String copyText) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("simple text", copyText);
            clipboard.setPrimaryClip(clip);

//            Toast.makeText(MainActivity.this, R.string.main_add_to_clipboard, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void bindGoogleFit() {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    accessGoogleFit();
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)) {
                    // TODO implement dialog of why the permisson is needed
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                            PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
                }
            } else {
                accessGoogleFit();
            }
        }

        @JavascriptInterface
        public void bindFitbit() {
            String url = "https://www.fitbit.com/oauth2/authorize?" +
                    "client_id=" + getString(R.string.connect_fitbit_client_id) + "&" +
                    "response_type=code" + "&" +
                    "scope=" + "activity%20sleep" + "&" +
                    "expires_in=31536000&prompt=login%20consent&redirect_uri=teamwalk" + getString(R.string.env) + "://webconnect?device=fitbit";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }

        @JavascriptInterface
        public void bindGarmin() {
            String oauthConsumerKey = getString(R.string.connect_garmin_consumer_key);
            String oauthSignatureMethod = "HMAC-SHA1";

            String oauthNonce = randomString();

            Date today = new Date();
            long timestamp = today.getTime() / 1000L;
            String oauthTimestamp = String.valueOf(timestamp);
            String oauthVersion = "1.0";

            String signature = "oauth_consumer_key=" + oauthConsumerKey + "&" +
                    "oauth_nonce=" + oauthNonce + "&" +
                    "oauth_signature_method=" + oauthSignatureMethod + "&" +
                    "oauth_timestamp=" + oauthTimestamp + "&" +
                    "oauth_version=" + oauthVersion;
            String signatureBaseString = "";
            try {
                String signatureBase = URLEncoder.encode(garmin_base_url + "request_token", "utf-8") + "&" + URLEncoder.encode(signature, "utf-8");
                signatureBaseString = "POST&" + signatureBase;
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "Fail to encode garmin url");
            }

            String keyString = getString(R.string.connect_garmin_comsumer_secret) + "&";
            String oauthSignature = "";
            try {
                oauthSignature =  URLEncoder.encode(sha1(signatureBaseString, keyString), "utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "Fail to encode fitbit url");
            } catch (NoSuchAlgorithmException e) {
                Log.d(TAG, "Fail to encode fitbit url");
            } catch (InvalidKeyException e) {
                Log.d(TAG, "Fail to encode fitbit url");
            }

            String authorization = "OAuth " + "oauth_version=\"" + oauthVersion + "\", " +
                    "oauth_consumer_key=\"" + oauthConsumerKey + "\", " +
                    "oauth_timestamp=\"" + oauthTimestamp + "\", " +
                    "oauth_nonce=\"" + oauthNonce + "\", " +
                    "oauth_signature_method=\"" + oauthSignatureMethod + "\", " +
                    "oauth_signature=\"" + oauthSignature + "\"";

            Toast connFailToast = Toast.makeText(MainActivity.this, R.string.onboard_connect_fail, Toast.LENGTH_LONG);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(garmin_base_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            GarminTokenService garminTokenService = retrofit.create(GarminTokenService.class);
            Call<ResponseBody> getAuthCodeCall = garminTokenService.getAuthCode(authorization);

            getAuthCodeCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    ResponseBody tokenString = response.body();
//                    String token = "";
//                    try {
//                        token = tokenString.string();
//                    } catch (IOException e) {
//                        Log.d(TAG, "Fail to get token");
//                    }
//
//                    String ts = token.substring(token.lastIndexOf("=") + 1, token.length());
//                    token = token.substring(0, token.indexOf("&"));
//
//                    if (TextUtils.isEmpty(token)) {
//                        connFailToast.show();
//                    } else {
//                        String url = "https://connect.garmin.com/oauthConfirm?" + token + "&" +
//                                "oauth_callback=teamwalk" + getString(R.string.env) + "://webconnect?device=garmin@" + ts;
//
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        startActivity(intent);
//                    }

                    try{
                        if(response.body()!=null){
                            String responseString = response.body().string();
                            tsGarmin = responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length());
                            responseString = responseString.substring(0, responseString.indexOf("&"));
                            if (TextUtils.isEmpty(responseString))
                                connFailToast.show();
                            else {
//                                String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
//                                        "oauth_callback=teamwalk" + getString(R.string.env) + "://webconnect?device=garmin@" + ts;
                                String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
                                        "oauth_callback=teamwalk" + getString(R.string.env) + "://webconnectgarmin";

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        }else
                            connFailToast.show();
                    }catch (Exception e){
                        connFailToast.show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    connFailToast.show();
                }
            });
        }

        @JavascriptInterface
        public void removePattern() {
            SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
            prefEditor.putBoolean(getString(R.string.pref_login_pattern_status), false);
            prefEditor.putInt(getString(R.string.pref_login_segment_control_pos), 0);
            prefEditor.apply();
        }

        @JavascriptInterface
        public void removeGoogleFit() {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN_FOR_DISABLE_FIT);
        }

        @JavascriptInterface
        public void signOut() {
            SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
            prefEditor.putBoolean(getString(R.string.pref_login_auth), false);
            prefEditor.putString(getString(R.string.pref_login_username), "");
            prefEditor.putString(getString(R.string.pref_login_ticket), "");
            prefEditor.putString(getString(R.string.pref_login_castgc), "");
            prefEditor.putString(getString(R.string.pref_login_token), "");
            prefEditor.putString(getString(R.string.pref_login_refresh_token), "");
            prefEditor.putLong(getString(R.string.pref_login_exp), 0L);
            prefEditor.apply();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://login"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        @JavascriptInterface
        public void syncGoogleFit(String token) {
            runOnUiThread(() -> {
                teamwalkToken = token;
                Calendar start = Calendar.getInstance();
                start.add(Calendar.DATE, -6);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                Calendar end = Calendar.getInstance();
                end.add(Calendar.DATE, 1);
                end.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.MINUTE, 0);
                end.set(Calendar.SECOND, 0);
                stepDataMap = new HashMap<>();
                caloriesDataMap = new HashMap<>();
                sleepDataList = new ArrayList<>();
                getStepData(start.getTimeInMillis(), end.getTimeInMillis());
            });
        }

        @JavascriptInterface
        public String saveDataToFile(String data, String fileName) {
            if (data.isEmpty() || fileName.isEmpty() || fileName.length() <= 0
                    || FortifyUtil.checkFileName(fileName)) {
                return null;
            } else {
                String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                String saveFile = "";
                FileOutputStream fileOutputStream = null;
                try {
//                fileName = "數位理賠進件同意書.docx";
                    byte[] fileBytes = Base64.decode(data.replaceFirst(
                            "data:text/xml;base64,", ""), 0);
                    fileOutputStream = new FileOutputStream(new File(savePath, fileName));
//                fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

                    fileOutputStream.write(fileBytes);
//                fileOutputStream.close();
                    saveFile = savePath + "/" + fileName;
//            }catch(Exception e){
//                saveFile = null;
//                e.printStackTrace();
//            }
                } catch (IOException ex) {
                    saveFile = null;
//                    ex.printStackTrace();
                } finally {
                    try {
                        if (fileOutputStream != null)
                            fileOutputStream.close();
                    } catch (IOException e) {
                        saveFile = null;
//                        e.printStackTrace();
                    }

                }
                return saveFile;
            }
        }
        @JavascriptInterface
        public boolean ShareContent(String shareJsonStr) {
//            if(shareUri!=null)
//                ShareUtil.deleteUri(MainActivity.this, shareUri);
//            shareUri=null;
//            ShareUtil.delShareImage(MainActivity.this);
            return ShareUtil.ShareContent(shareJsonStr, MainActivity.this);
        }

        @JavascriptInterface
        public boolean runScoreGooglePlay() {
            return scoreGooglePlay();
        }

        @JavascriptInterface
        public void reloadPage() {
            clearCache = true;
        }
    }

    /**
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }

        return super.onKeyDown(keyCode, event);
    }


    public boolean isDeviceRooted() {
        String su = "su";
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/",
                "/system/sbin/", "/usr/bin/", "/vendor/bin/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    private boolean validateSign(){
        try {
            //得到签名
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;

            //将签名文件MD5编码一下
            String signStr  = bytes2HexString(hashTemplate(signs[0].toByteArray())).replaceAll("(?<=[0-9A-F]{2})[0-9A-F]{2}", ":$0");
            //将应用现在的签名MD5值和我们正确的MD5值对比
            String env = getString(R.string.env);
            if (env.equals("debug") || env.equals("uat")) {
                return "57:74:43:AE:56:A2:0C:F8:C4:1C:3C:35:E5:57:7A:D7".equals(signStr);
            } else {
                return "30:8F:64:63:58:93:47:BE:91:48:69:09:57:7D:B3:52".equals(signStr);
            }
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
        }
        return false;
    }

//    public static String md5(String inStr){
//
//        MessageDigest md5 = null;
//        try {
//            md5 = MessageDigest.getInstance("MD5");
//        } catch (NoSuchAlgorithmException e) {
////            System.out.println(e.toString());
////            e.printStackTrace();
//            return "";
//        }
//
//        char[] charArray = inStr.toCharArray();
//        byte[] byteArray = new byte[charArray.length];
//
//        for (int i = 0; i < charArray.length; i++)
//            byteArray[i] = (byte) charArray[i];
//
//        byte[] md5Bytes = md5.digest(byteArray);
//        StringBuffer hexValue = new StringBuffer();
//        for (int i = 0; i < md5Bytes.length; i++){
//
//            int val = ((int) md5Bytes[i]) & 0xff;
//            if (val < 16) {
//                hexValue.append("0");
//            }
//            hexValue.append(Integer.toHexString(val));
//        }
//
//        return hexValue.toString();
//
//    }

    private static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int len = bytes.length;
        if (len <= 0) {
            return "";
        }
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEX_DIGITS[bytes[i] >> 4 & 0x0f];
            ret[j++] = HEX_DIGITS[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static byte[] hashTemplate(final byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
            return null;
        }
    }

    private void setPwPageFlag(boolean pwPageFlag){
        SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
        prefEditor.putBoolean("Teamwalk_PwPageFlag", pwPageFlag);
        prefEditor.apply();
    }

    private void showPwErrorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("提醒您：因已持續登入一段時間，需重新登入才能變更密碼");
        builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

        //设置正面按钮
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                toLogin();
                setPwPageFlag(true);
                SecuredPreferenceStore.Editor prefEditor = loginSharedPref.edit();
                prefEditor.putBoolean(getString(R.string.pref_login_auth), false);
                prefEditor.putString(getString(R.string.pref_login_username), "");
                prefEditor.putString(getString(R.string.pref_login_ticket), "");
                prefEditor.putString(getString(R.string.pref_login_castgc), "");
                prefEditor.putString(getString(R.string.pref_login_token), "");
                prefEditor.putString(getString(R.string.pref_login_refresh_token), "");
                prefEditor.putLong(getString(R.string.pref_login_exp), 0L);
                prefEditor.apply();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://login"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void illegalApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("");
        builder.setMessage("提醒您，請勿任意竄改程式碼，已停止您的系統使用權限，謝謝");
        builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

        //设置正面按钮
//        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getStepData(long start, long end) {
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName("com.google.android.gms")
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(dataSource)
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .build();
        if (GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null) {
            HistoryClient historyClient =  Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(getApplicationContext()));
            historyClient.readData(readRequest)
                    .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                        @Override
                        public void onSuccess(DataReadResponse dataReadResponse) {
                            dumpDataSet(dataReadResponse, FIELD_STEPS);
                            getCaloriesData(start, end);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(MainActivity.TAG, "google fit read data failed");
                        }
                    });
        }
    }

    private void getCaloriesData(long start, long end) {
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .build();
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        dumpDataSet(dataReadResponse, FIELD_CALORIES);
                        getSleepData(start, end);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Google Fit: " + e);
                    }
                });
    }

    private void getSleepData(long start, long end) {
        SessionReadRequest request = new SessionReadRequest.Builder()
                .readSessionsFromAllApps()
                // By default, only activity sessions are included, so it is necessary to explicitly
                // request sleep sessions. This will cause activity sessions to be *excluded*.
                .includeSleepSessions()
                // Sleep segment data is required for details of the fine-granularity sleep, if it is present.
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeInterval(start, end, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getSessionsClient(this, GoogleSignIn.getLastSignedInAccount(this)).readSession(request)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        for (Session session : sessionReadResponse.getSessions()) {
                            long startTs = session.getStartTime(TimeUnit.MILLISECONDS);
                            long endTs = session.getEndTime(TimeUnit.MILLISECONDS);
                            Map<String, Long> map = new HashMap<>();
                            map.put("startTs", startTs);
                            map.put("endTs", endTs);
                            sleepDataList.add(map);
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("steps", stepDataMap.values());
                        map.put("calories", caloriesDataMap.values());
                        map.put("sleep", sleepDataList);
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(getString(R.string.api_url))
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        UserService userService = retrofit.create(UserService.class);
                        Call<Object> call = userService.syncGooglefit(teamwalkToken, map);
                        call.enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                            }

                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                            }
                        });
                    }
                });
    }

    private void dumpDataSet(DataReadResponse dataReadResponse,Field field) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        for (Bucket bucket : dataReadResponse.getBuckets()) {
            for (DataSet dataSet : bucket.getDataSets()) {
                for (DataPoint dp : dataSet.getDataPoints()) {
                    if ("user_input".equals(dp.getOriginalDataSource().getStreamName())) {
//                        String str = String.format("user_input: %s %s %s", timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)),timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)),dp.getValue(field).toString());
//                        Log.d(MainActivity.TAG, str);
                        continue;
                    }
                    long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                    String dayAt = format.format(startTime);
//                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    if (field == Field.FIELD_STEPS) {
//                        Log.d(MainActivity.TAG, "real_step: "+ timeFormat.format(startTime) +" " + dp.getValue(field).toString());
                        int value = dp.getValue(field).asInt();
                        if (stepDataMap.get(dayAt) == null) {
                            FitStep fitStep = new FitStep(dayAt);
                            fitStep.setSteps(value);
                            stepDataMap.put(dayAt, fitStep);
                        } else {
                            FitStep fitStep =  stepDataMap.get(dayAt);
                            fitStep.setSteps(fitStep.getSteps() + value);
                        }
                    } else {
                        float value = dp.getValue(field).asFloat();
                        if (caloriesDataMap.get(dayAt) == null) {
                            FitCalories fitCalories = new FitCalories(dayAt);
                            fitCalories.setCalories(value);
                            caloriesDataMap.put(dayAt,fitCalories);
                        } else {
                            FitCalories fitCalories = caloriesDataMap.get(dayAt);
                            fitCalories.setCalories(fitCalories.getCalories() + value);
                        }
                    }
                }
            }
        }
    }

//    //动态获取儲存權限
//    private static final int PERMISSIONS_EXTERNAL_STORAGE_RECOGNITION = 1011;
//    private static String[] PERMISSIONS_STORAGE = {
////            Manifest.permission.INTERNET,
////            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//    public static void verifyStoragePermissions(Activity activity) {
//        try {
//            //检测是否有写的权限
////            PermissionChecker.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
//            int permission = ActivityCompat.checkSelfPermission(activity,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            if (permission != PackageManager.PERMISSION_GRANTED) {
//                // 没有写的权限，去申请写的权限，会弹出对话框
//                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,PERMISSIONS_EXTERNAL_STORAGE_RECOGNITION);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private final int permissionRequestCode = 1011;//权限请求码
//    private List<String> permissionList = new ArrayList<>();
//    private boolean checkPermission() {
//        boolean hasPermissionDismiss = false;
//        permissionList.clear();
//        permissionList.add(Manifest.permission.INTERNET);
//        permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
//        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        //逐个判断你要的权限是否已经通过
//        for (int i = 0; i < permissionList.size(); i++) {
//            if (ContextCompat.checkSelfPermission(this, permissionList.get(i)) != PackageManager.PERMISSION_GRANTED) {
//                hasPermissionDismiss = true;
//            }
//        }
//        //申请权限
//        if (hasPermissionDismiss) {//有权限没有通过，需要申请
//            Log.i(TAG, "hasPermission: requestPermissions");
//            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), permissionRequestCode);
//        }
//        Log.i("CelebrusCSAUtil", "checkPermission: ");
//
//        return !hasPermissionDismiss;
//    }
//    private void onCheckPermission(String[] permissions, int[] grantResults){
//        List<String> errorpermission = new ArrayList<>();
//        for (int i = 0; i < grantResults.length; i++) {
//            if (grantResults[i] == -1) {
//                errorpermission.add(permissionList.get(i));
//            }
//        }
//
//        if(errorpermission.size()>0){    //如果有权限没有被允许
//            for (String onePermissiion: permissions) {
//                switch (onePermissiion){
//                    case Manifest.permission.READ_EXTERNAL_STORAGE:
//                        Toast.makeText(MainActivity.this, "讀取權限申請失敗!", Toast.LENGTH_LONG).show();
//                        break;
//                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
//                        Toast.makeText(MainActivity.this, "儲存權限申請失敗!", Toast.LENGTH_LONG).show();
//                        break;
//                    case Manifest.permission.INTERNET:
//                    case Manifest.permission.ACCESS_NETWORK_STATE:
//                        Toast.makeText(MainActivity.this, "網絡權限申請失敗!", Toast.LENGTH_LONG).show();
//                        break;
//                    case Manifest.permission.ACCESS_COARSE_LOCATION:
//                    case Manifest.permission.ACCESS_FINE_LOCATION:
//                        Toast.makeText(MainActivity.this, "位置權限申請失敗!", Toast.LENGTH_LONG).show();
//                        break;
//                }
//            }
//        }else{
//            Toast.makeText(MainActivity.this, "權限申請成功!", Toast.LENGTH_LONG).show();
//        }
//    }

    private GooglePlayCore googlePlayCore = null;
    private void initGooglePlay(){
        Log.i("GooglePlayCore", "initGooglePlay: ");
        if(googlePlayCore==null)
            googlePlayCore = new GooglePlayCore(this);
    }
    private boolean scoreGooglePlay(){
        Log.i("GooglePlayCore", "scoreGooglePlay: ");
        boolean retVal = false;
        if(googlePlayCore.canScore()){
            googlePlayCore.scoreGooglePlay();
            retVal = true;
        }
        return retVal;
    }

    private void launchCamera() {
        // 建立啟動相機的 Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 確認裝置有相機應用可以處理該 Intent
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 建立存放照片的臨時檔案
            File photoFile = null;
            try {
                photoFile = createImageFile();  // 建立圖片檔案 (儲存在應用的專屬目錄)
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                // 取得此檔案對應的 content:// URI
                // 注意：authority 必須與 AndroidManifest.xml 中設定的相符 (以下使用當前應用的套件名稱)
                cameraImageUri = FileProvider.getUriForFile
                        (
                                this,
                                getPackageName() + ".fileprovider",
                                photoFile
                        );
                // 將該 URI 作為額外輸出參數提供給相機應用
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            }
            // 啟動相機應用，等待結果返回 onActivityResult
            startActivityForResult(cameraIntent, FILE_CHOOSER_REQUEST_CODE);
        }
    }

    /**
     * 建立圖片檔案的方法，命名包含時間戳避免重複。
     * 檔案儲存在應用專屬的圖片目錄 (無需請求外部存取權限)。
     */
    private File createImageFile() throws IOException {
        // 使用時間戳建立唯一的檔名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // 取得應用在外部儲存體的圖片目錄 (例如: /storage/emulated/0/Android/data/應用包名/files/Pictures)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // 建立臨時的圖片檔案
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }

    // 加入此方法，取代 AlertDialog，改用 BottomSheetDialog 顯示於下方
    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);

        bottomSheetDialog.findViewById(R.id.choose_image).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            pickImage();
        });

        bottomSheetDialog.findViewById(R.id.take_photo).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                launchCamera();
            }
        });

        bottomSheetDialog.findViewById(R.id.choose_file).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            pickFile();
        });

        bottomSheetDialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
        });

        bottomSheetDialog.setOnCancelListener(dialog -> {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
        });

        bottomSheetDialog.show();
    }

    // 選擇圖片
    private void pickImage() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "選擇圖片");
        startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE);
    }

    // 選擇檔案
    private void pickFile() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        fileIntent.setType("*/*");
        Intent fileChooser = Intent.createChooser(fileIntent, "選擇檔案");
        startActivityForResult(fileChooser, FILE_CHOOSER_REQUEST_CODE);
    }
}