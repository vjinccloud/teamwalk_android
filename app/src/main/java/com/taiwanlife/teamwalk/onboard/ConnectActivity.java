/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.onboard;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.Task;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.model.AuthToken;
import com.taiwanlife.teamwalk.model.FitbitToken;
import com.taiwanlife.teamwalk.model.HealthDevice;
import com.taiwanlife.teamwalk.service.AuthenticateService;
import com.taiwanlife.teamwalk.service.FitbitTokenService;
import com.taiwanlife.teamwalk.service.GarminTokenService;
import com.taiwanlife.teamwalk.service.HealthDeviceService;
import com.taiwanlife.teamwalk.util.AlertDialogFragment;
import com.taiwanlife.teamwalk.util.GsonCreator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.taiwanlife.teamwalk.util.Utilities.randomString;
import static com.taiwanlife.teamwalk.util.Utilities.sha1;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class ConnectActivity extends OnboardActivity implements AlertDialogFragment.Builder {

    private static final String TAG = "ConnectActivity";

    private static final int PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 1;
    public static final int GOOGLE_SIGN_IN = 2;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 3;

//    private SharedPreferences loginSharedPref;

    private CheckBox googleCheckbox;
    private CheckBox fitbitCheckbox;
    private CheckBox garminCheckbox;

    private boolean googleBinded = false;
    private boolean fitbitBinded = false;
    private boolean garminBinded = false;

    private GoogleSignInClient googleSignInClient;
    private String authCode;
    private String fitbit_base_url = "https://api.fitbit.com/oauth2/";
    private String garmin_base_url = "https://connectapi.garmin.com/oauth-service/oauth/";
    private static String tsGarmin = "";

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        setSkip();
        setDialog();

        googleCheckbox = findViewById(R.id.onboarding_checkbox_googleFit);
        fitbitCheckbox = findViewById(R.id.onboarding_checkbox_fitbit);
        garminCheckbox = findViewById(R.id.onboarding_checkbox_garmin);

        setGoogleFitSwitch();
        setFitbitSwitch();
        setGarminSwitch();

        setNext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUser();

//        googleCheckbox.setChecked(false);
//        fitbitCheckbox.setChecked(false);
//        garminCheckbox.setChecked(false);

        Uri uri = getIntent().getData();
        if (uri != null) {
            String scheme_action = uri.getHost();
//            String scheme_action = uri.getQueryParameter("device");
            Toast connFailToast = Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG);
            if (scheme_action.equals("connect")) {
                Log.i(TAG, "connect fitbit");
//                Log.d(TAG, uri.getQueryParameter("code"));
                String code = uri.getQueryParameter("code");
                String error = uri.getQueryParameter("error");
                if (code == null && error != null) {
                    connFailToast.show();
                    return;
                }
                String encodeAuthString = getString(R.string.connect_fitbit_client_id) + ":" + getString(R.string.connect_fitbit_client_secret);
                String authorizationValue = "Basic " + Base64.encodeToString(encodeAuthString.getBytes(), Base64.NO_WRAP);

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
                        "teamwalk" + getString(R.string.env) + "://connect?device=fitbit");

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
                            HealthDevice healthDevice = new HealthDevice("Fitbit", fitbitToken.getAccessToken(), fitbitToken.getRefreshToken());
                            fitbitBinded = true;
                            bindDevice(healthDevice, fitbitCheckbox);
                        }
                    }

                    @Override
                    public void onFailure(Call<FitbitToken> call, Throwable t) {
                        connFailToast.show();
                    }
                });

                getIntent().setData(null);
            } else if (scheme_action.equals("connectGarmin")) {
                Log.d(TAG, "connect garmin");

//                Log.d(TAG, uri.getQueryParameter("oauth_token"));
//                Log.d(TAG, uri.getQueryParameter("oauth_verifier"));

//                String oauthTokenSecret = scheme_action.substring(scheme_action.indexOf("@") + 1, scheme_action.length());
                String oauthTokenSecret = tsGarmin;
                String oauthConsumerKey = getString(R.string.connect_garmin_consumer_key);
                String oauthToken = uri.getQueryParameter("oauth_token");
                String oauthSignatureMethod = "HMAC-SHA1";

                String oauthNonce = randomString();

                Date today = new Date();
                long timestamp = today.getTime() / 1000L;
                String oauthTimestamp = String.valueOf(timestamp);
                String oauthVersion = "1.0";
                String oauthVerifier = uri.getQueryParameter("oauth_verifier");
                if (oauthVerifier == null || oauthVerifier.equals("null")) {
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
//                    Log.e(TAG, "Fail to encode signature string", e);
                }

                String keyString = getString(R.string.connect_garmin_comsumer_secret) + "&" + oauthTokenSecret;
                String oauthSignature = "";
                try {
                    oauthSignature = URLEncoder.encode(sha1(signatureBaseString, keyString), "utf-8");
                } catch (UnsupportedEncodingException e) {
//                    Log.e(TAG, "Fail to encode oauth signature string", e);
                } catch (NoSuchAlgorithmException e) {
//                    Log.e(TAG, "Fail to encode oauth signature string", e);
                } catch (InvalidKeyException e) {
//                    Log.e(TAG, "Fail to encode oauth signature string", e);
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
//                        ResponseBody tokenString = response.body();
//                        String token = "";
//                        try {
//                            token = tokenString.string();
//                        } catch (IOException e) {
//                            Log.d(TAG, "Fail to get token");
//                        }
//
//                        if (TextUtils.isEmpty(token)) {
//                            connFailToast.show();
//                        } else {
//                            String oauthToken = token.substring(token.indexOf("=") + 1, token.indexOf("&"));
//                            String oauthTokenSecret = token.substring(token.lastIndexOf("=") + 1, token.length());
//
//                            HealthDevice healthDevice = new HealthDevice("Garmin", oauthToken, oauthTokenSecret);
//                            garminBinded = true;
//                            bindDevice(healthDevice, garminCheckbox);
//                        }

                        try {
                            if (response.body() != null) {
                                String responseString = response.body().string();
                                if (TextUtils.isEmpty(responseString))
                                    connFailToast.show();
                                else {

                                    String oauthToken = responseString.substring(responseString.indexOf("=") + 1, responseString.indexOf("&"));
                                    String oauthTokenSecret = responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length());

                                    HealthDevice healthDevice = new HealthDevice("Garmin", oauthToken, oauthTokenSecret);
                                    garminBinded = true;
                                    bindDevice(healthDevice, garminCheckbox);
                                }
                            } else
                                connFailToast.show();
                        } catch (Exception e) {
                            connFailToast.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        connFailToast.show();
                    }
                });

                getIntent().setData(null);
            }
//            String device = scheme_action;
//            if (scheme_action.indexOf("@") > 0) {
//                device = scheme_action.substring(0, scheme_action.indexOf("@"));
//            }
//
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

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
                        Log.i(TAG, "Fail to connect google fit", e);
                        Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG).show();
                    }

                    if (TextUtils.isEmpty(authCode)) {
                        Log.i(TAG, "Fail to connect google fit, no auth code");
                        Toast.makeText(this, R.string.onboard_connect_fail, Toast.LENGTH_LONG).show();
                    } else {
                        HealthDevice healthDevice = new HealthDevice("Google FIT", "", authCode);
                        googleBinded = true;
                        bindDevice(healthDevice, googleCheckbox);
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
                    HealthDevice healthDevice = new HealthDevice("Google FIT", "", authCode);
                    googleBinded = true;
                    bindDevice(healthDevice, googleCheckbox);
                }
            }
        }
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
            default:
                return;
        }
    }

    private void setSkip() {
        Button skipButton = findViewById(R.id.onboarding_skip_button);
        skipButton.setOnClickListener((View v) -> {
            if (getMyUser() != null) {
                getMyUser().setCompleteOnboarding(true);
                updateUser(getMyUser(), true);
            }

            Intent backToMainIntent = new Intent(this, MainActivity.class);
            backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            backToMainIntent.setAction(MainActivity.ON_BOARD_FINISH);
            startActivity(backToMainIntent);
            finish();
        });
    }

    private void setDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.fragment_dialog, null);

        TextView infoText = dialogView.findViewById(R.id.onboarding_dialog_textContainer);
        infoText.setText(Html.fromHtml(getResources().getString(R.string.onboard_connect_info), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));

        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        dialog.setContentView(dialogView);

        ImageButton infoButton = findViewById(R.id.onboarding_imageView_info);
        infoButton.setOnClickListener((View v) -> {
//            dialog.show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.onboard_connect_message));
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setCancelable(false);            //点击对话框以外的区域是否让对话框消失

            //设置正面按钮
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        Button closeButton = dialogView.findViewById(R.id.onboarding_dialog_close);
        closeButton.setOnClickListener((View v) -> {
            dialog.dismiss();
        });
    }

    private void setGoogleFitSwitch() {
        googleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!googleBinded) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            if (ContextCompat.checkSelfPermission(ConnectActivity.this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                                accessGoogleFit();
                            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)) {
                                // TODO implement dialog of why the permisson is needed
                            } else {
                                ActivityCompat.requestPermissions(ConnectActivity.this,
                                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                                        PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
                            }
                        } else {
                            accessGoogleFit();
                        }
                    }
                } else {
                    if (googleBinded) {
                        removeAllDevices();
                    }
                }
            }
        });
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
        noticeChangeDevice("Google Fit", googleSignInClient.getSignInIntent());
    }

    private void setFitbitSwitch() {
        fitbitCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!fitbitBinded) {
                        String url = "https://www.fitbit.com/oauth2/authorize?" +
                                "client_id=" + getString(R.string.connect_fitbit_client_id) + "&" +
                                "response_type=code" + "&" +
                                "scope=" + "activity%20sleep" + "&" +
                                "expires_in=31536000&prompt=login%20consent&redirect_uri=teamwalk" + getString(R.string.env) + "://connect?device=fitbit";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                        noticeChangeDevice("Fitbit", intent);
                    }
                } else {
                    if (fitbitBinded) {
                        removeAllDevices();
                    }
                }
            }
        });
    }

    private void setGarminSwitch() {
        garminCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!garminBinded) {
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
                            oauthSignature = URLEncoder.encode(sha1(signatureBaseString, keyString), "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            Log.d(TAG, "Fail to encode garmin url");
                        } catch (NoSuchAlgorithmException e) {
                            Log.d(TAG, "Fail to encode garmin url");
                        } catch (InvalidKeyException e) {
                            Log.d(TAG, "Fail to encode garmin url");
                        }

                        String authorization = "OAuth " + "oauth_version=\"" + oauthVersion + "\", " +
                                "oauth_consumer_key=\"" + oauthConsumerKey + "\", " +
                                "oauth_timestamp=\"" + oauthTimestamp + "\", " +
                                "oauth_nonce=\"" + oauthNonce + "\", " +
                                "oauth_signature_method=\"" + oauthSignatureMethod + "\", " +
                                "oauth_signature=\"" + oauthSignature + "\"";

                        Toast connFailToast = Toast.makeText(ConnectActivity.this, R.string.onboard_connect_fail, Toast.LENGTH_LONG);

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(garmin_base_url)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        GarminTokenService garminTokenService = retrofit.create(GarminTokenService.class);
                        Call<ResponseBody> getAuthCodeCall = garminTokenService.getAuthCode(authorization);

                        getAuthCodeCall.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                ResponseBody tokenString = response.body();
//                                String token = "";
//                                try {
//                                    token = tokenString.string();
//                                } catch (IOException e) {
////                                    e.printStackTrace();
//                                    Log.d(TAG, "Fail to get garmin auth code");
//                                }
//
//                                String ts = token.substring(token.lastIndexOf("=") + 1, token.length());
//                                token = token.substring(0, token.indexOf("&"));
//
//                                if (TextUtils.isEmpty(token)) {
//                                    connFailToast.show();
//                                } else {
//                                    String url = "https://connect.garmin.com/oauthConfirm?" + token + "&" +
//                                            "oauth_callback=teamwalk" + getString(R.string.env) + "://connect?device=garmin@" + ts;
//                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//
//                                    noticeChangeDevice("Garmin", intent);
//                                }

                                try{
                                    if(response.body()!=null){
                                        String responseString = response.body().string();
                                        tsGarmin = responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length());
                                        responseString = responseString.substring(0, responseString.indexOf("&"));
                                        if (TextUtils.isEmpty(responseString))
                                            connFailToast.show();
                                        else {
//                                            String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
//                                                    "oauth_callback=teamwalk" + getString(R.string.env) + "://connectGarmin?device=garmin@" + ts;
                                            String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
                                                    "oauth_callback=teamwalk" + getString(R.string.env) + "://connectGarmin";
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                                            noticeChangeDevice("Garmin", intent);
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
                } else {
                    if (garminBinded) {
                        removeAllDevices();
                    }
                }
            }
        });
    }

    public void bindDevice(HealthDevice healthDevice, CheckBox switchObject) {

        if (TextUtils.isEmpty(getT())) {
//            Log.e(TAG, "Auth token not found");
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.api_url))
                    .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))))
                    .build();

            HealthDeviceService healthDeviceService = retrofit.create(HealthDeviceService.class);

            Toast connectSuccessToast = Toast.makeText(this, getString(R.string.onboard_connect_success), Toast.LENGTH_LONG);
            Toast connFailToast = Toast.makeText(this, getString(R.string.access_connect_fail), Toast.LENGTH_LONG);

            Calendar now = Calendar.getInstance();
            Calendar expire = Calendar.getInstance();
            expire.setTimeInMillis(getExp());
            if (now.before(expire)) {
                Call<Map> bindHealthDeviceCall = healthDeviceService.bindHealthDevice(getT(), healthDevice);
                bindHealthDeviceCall.enqueue(new Callback<Map>() {
                    @Override
                    public void onResponse(Call<Map> call, Response<Map> response) {
                        Map resp = (Map) response.body();

                        if (resp == null) {
                            connFailToast.show();
                        } else {
                            if (((double) resp.getOrDefault("status_code", 0)) == ((double) 200)) {
                                switchObject.setChecked(true);
                                connectSuccessToast.show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map> call, Throwable t) {
                        connFailToast.show();
                    }
                });
            } else {
                RequestBody formBody = new FormBody.Builder()
                        .add("username", getUname())
                        .add("token", getT())
                        .add("refreshToken", getRt())
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
                            connFailToast.show();
                        } else {
                            if (authToken.getCode() == 201) {

                                Call<Map> bindHealthDeviceCall = healthDeviceService.bindHealthDevice(authToken.getToken(), healthDevice);
                                bindHealthDeviceCall.enqueue(new Callback<Map>() {
                                    @Override
                                    public void onResponse(Call<Map> call, Response<Map> response) {
                                        Map resp = (Map) response.body();

                                        if (resp == null) {
                                            connFailToast.show();
                                        } else {
                                            if (((double) resp.getOrDefault("status_code", 0)) == ((double) 200)) {
                                                switchObject.setChecked(true);
                                                connectSuccessToast.show();
                                            } else {
                                                connFailToast.show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Map> call, Throwable t) {
                                        connFailToast.show();
                                    }
                                });
                            } else {
                                connFailToast.show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthToken> call, Throwable t) {
                        connFailToast.show();
                    }
                });
            }
        }
    }

    public void noticeChangeDevice(String toDevice, Intent connect) {
        String fromDevice = "";
        if (googleCheckbox.isChecked() && googleBinded) {
            fromDevice = "Google Fit";
        }

        if (fitbitCheckbox.isChecked() && fitbitBinded) {
            fromDevice = "Fitbit";
        }

        if (garminCheckbox.isChecked() && garminBinded) {
            fromDevice = "Garmin";
        }

        if (TextUtils.isEmpty(fromDevice) || TextUtils.equals(fromDevice, toDevice)) {
            if (TextUtils.equals(toDevice, "Google Fit")) {
                startActivityForResult(connect, GOOGLE_SIGN_IN);
            } else {
                startActivity(connect);
            }
        } else {
            AlertDialogFragment dialogFragment = null;
            if (TextUtils.equals(toDevice, "Google Fit")) {
                dialogFragment = createDialogWithPositionBtn(getString(R.string.onboard_connect_change_title), getString(R.string.onboard_connect_change_body) + toDevice + "?", R.drawable.alert_1, getString(R.string.cancel), getString(R.string.ok), connect, true, GOOGLE_SIGN_IN);
                dialogFragment.setPrepareIntent(new AlertDialogFragment.PrepareIntent() {
                    @Override
                    public void preparePositive() {
                        removeAllDevices();
                    }

                    @Override
                    public void prepareNegative() {
                        googleCheckbox.setChecked(false);
                    }
                });
            } else {
                dialogFragment = createDialogWithPositionBtn(getString(R.string.onboard_connect_change_title), getString(R.string.onboard_connect_change_body) + toDevice + "?", R.drawable.alert_1, getString(R.string.cancel), getString(R.string.ok), connect, false, 0);
                dialogFragment.setPrepareIntent(new AlertDialogFragment.PrepareIntent() {
                    @Override
                    public void preparePositive() {
                        removeAllDevices();
                    }

                    @Override
                    public void prepareNegative() {
                        if (toDevice.equals("Fitbit")) {
                            fitbitCheckbox.setChecked(false);
                        } else if (toDevice.equals("Garmin")) {
                            garminCheckbox.setChecked(false);
                        }
                    }
                });
            }

            dialogFragment.show(getSupportFragmentManager(), TAG);
        }
    }

    private void removeAllDevices() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))))
                .build();

        HealthDeviceService healthDeviceService = retrofit.create(HealthDeviceService.class);

        Toast connectSuccessToast = Toast.makeText(this, getString(R.string.onboard_connect_remove_success), Toast.LENGTH_LONG);
        Toast connFailToast = Toast.makeText(this, getString(R.string.access_connect_fail), Toast.LENGTH_LONG);

        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        expire.setTimeInMillis(getExp());
        if (now.before(expire)) {
//            RequestBody noimei = new FormBody.Builder()
//                    .add("imei", "")
//                    .build();
            Map<String, Object> noimei = new HashMap<>();
            noimei.put("imei", "");
            Call<Map> bindHealthDeviceCall = healthDeviceService.removeHealthDevices(getT(), noimei);
            bindHealthDeviceCall.enqueue(new Callback<Map>() {
                @Override
                public void onResponse(Call<Map> call, Response<Map> response) {
                    Map resp = (Map) response.body();

                    if (resp == null) {
                        connFailToast.show();
                    } else {
                        if (((double) resp.getOrDefault("status_code", 0)) == ((double) 200)) {
                            googleBinded = false;
                            googleCheckbox.setChecked(false);

                            fitbitBinded = false;
                            fitbitCheckbox.setChecked(false);

                            garminBinded = false;
                            garminCheckbox.setChecked(false);

                            connectSuccessToast.show();
                        } else {
                            connFailToast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map> call, Throwable t) {
                    connFailToast.show();
                }
            });
        } else {
            RequestBody formBody = new FormBody.Builder()
                    .add("username", getUname())
                    .add("token", getT())
                    .add("refreshToken", getRt())
                    .build();

            Retrofit authRetrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.api_url))
                    .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))))
                    .build();

            AuthenticateService authenticateService = authRetrofit.create(AuthenticateService.class);
//            Uri parse = Uri.parse(getString(R.string.web_url));
//            String origin = parse.getScheme() + parse.getHost();
            Call<AuthToken> authTokenCall = authenticateService.authToken(getString(R.string.origin), formBody);
            authTokenCall.enqueue(new Callback<AuthToken>() {
                @Override
                public void onResponse(Call<AuthToken> call, Response<AuthToken> response) {
                    AuthToken authToken = (AuthToken) response.body();
                    if (authToken == null) {
                        connFailToast.show();
                    } else {
                        if (authToken.getCode() == 201) {

//                            RequestBody noimei = new FormBody.Builder()
//                                    .add("imei", "")
//                                    .build();
                            Map<String, Object> noimei = new HashMap<>();
                            noimei.put("imei", "");
                            Call<Map> bindHealthDeviceCall = healthDeviceService.removeHealthDevices(authToken.getToken(), noimei);
                            bindHealthDeviceCall.enqueue(new Callback<Map>() {
                                @Override
                                public void onResponse(Call<Map> call, Response<Map> response) {
                                    Map resp = (Map) response.body();

                                    if (resp == null) {
                                        connFailToast.show();
                                    } else {
                                        if (((double) resp.getOrDefault("status_code", 0)) == ((double) 200)) {
                                            googleBinded = false;
                                            googleCheckbox.setChecked(false);

                                            fitbitBinded = false;
                                            fitbitCheckbox.setChecked(false);

                                            garminBinded = false;
                                            garminCheckbox.setChecked(false);

                                            connectSuccessToast.show();
                                        } else {
                                            connFailToast.show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map> call, Throwable t) {
                                    connFailToast.show();
                                }
                            });
                        } else {
                            connFailToast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<AuthToken> call, Throwable t) {
                    connFailToast.show();
                }
            });
        }
    }

    private void setNext() {
        Button nextButton = findViewById(R.id.onboarding_next_button);
        nextButton.setOnClickListener((View view) -> {
            if (getMyUser() != null) {
                updateUser(getMyUser(), true);
            }

            Intent intent = new Intent(this, PromoteActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public AlertDialogFragment createDialog(String title, String msg, int image, String buttonText) {
        return AlertDialogFragment.newInstance(title, msg, image, buttonText);
    }

    @Override
    public AlertDialogFragment createDialogWithPositionBtn(String title, String msg, int image, String buttonText, String positiveButtonText, Intent positiveIntent, boolean forResult, int resultCode) {
        AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(title, msg, image, buttonText);
        dialogFragment.setPositiveButton(positiveButtonText, positiveIntent, forResult, resultCode);
        return dialogFragment;
    }

    @Override
    public AlertDialogFragment createDialogWithNegativeButton(String title, String msg, int image, String negativeText, Intent negativeIntent, boolean forResult, int resultCode) {
        return AlertDialogFragment.newInstance(title, msg, image, negativeText);
    }
}