package com.taiwanlife.teamwalk.ui.main.old_logic;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.taiwanlife.teamwalk.BuildConfig;
import com.taiwanlife.teamwalk.Config;
import com.taiwanlife.teamwalk.EnvironmentConfig;
import com.taiwanlife.teamwalk.EnvironmentManager;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.java_utils.FortifyUtil;
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager;
import com.taiwanlife.teamwalk.utils.WebViewFileWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import timber.log.Timber;

public class MainWebViewJava extends WebView {
    public interface SimpleCallback {
        void callback();
    }

    public interface MainWebViewJavaCallback {
        void showPwErrorDialog();

        void enableLoading(Boolean enable);

        void enableLoadingText(Boolean enable, int colorOfText);

        void showBottomSheetDialog(WebViewFileWrapper webViewFileWrapper);

        void getPermissionAndCallback(ArrayList<String> permissions, SimpleCallback simpleCallback);

        void accessGoogleFit();

        void removeGoogleFit();

        boolean scoreGooglePlay();

        void getStepData(long start, long end);

        void toLogin();

        boolean shareContent(String shareJsonStr);

        void callGetGarminAuthCode();

        // region 參數
        @Nullable
        String getPid();

        void setPid(@Nullable String pid);

        @Nullable
        Boolean getClearCache();

        void setClearCache(@Nullable Boolean clearCache);

        @Nullable
        String getFid();

        void setFid(@Nullable String fid);

        @Nullable
        String getTsGarmin();

        void setTsGarmin(@Nullable String tsGarmin);

        @Nullable
        String getFCMToken();

        void setFCMToken(@Nullable String FCMToken);

        @Nullable
        String getTicket();

        void setTicket(@Nullable String ticket);
        // end region
    }

    // 修改新增之參數
    private WebView webView = this;
    private EnvironmentConfig environmentConfig = EnvironmentManager.INSTANCE.getEnvironmentConfig();
    private String env = BuildConfig.BUILD_TYPE;
    private MainWebViewJavaCallback mainWebViewJavaCallback;
    // 只有在這個webview的範圍使用的參數 不用上到MainActivity
    private String teamwalkToken;
    private String fcmToken;

    // 本來就有的參數 這邊先做紀錄
    // private String pid
    // private String clearCache
    // private String fid
    // private String ticket
    // private String fcmToken
    // private String tsGarmin
    // private String teamwalkToken
    // private HashMap<> stepDataMap
    // private HashMap<> caloriesDataMap
    // private ArrayList<> sleepDataList
    // private HashMap<> getStepData

    public MainWebViewJava(@NonNull Context context) {
        super(context);
    }

    public MainWebViewJava(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MainWebViewJava(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MainWebViewJava(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setUp(MainWebViewJavaCallback mainWebViewJavaCallback) {
        this.mainWebViewJavaCallback = mainWebViewJavaCallback;
        setMainWebView();
    }


    private void setMainWebView() {
//        webView = new WebView(this);
//        setContentView(webView);
//        webView = findViewById(R.id.main_webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "/env=taiwanlife_teamwalk_app");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        webSettings.setBuiltInZoomControls(false);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            webSettings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        // if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //     WebView.setWebContentsDebuggingEnabled(true);
        // }


        webView.setWebViewClient(new MainWebViewClient(getContext()));
        webView.setWebChromeClient(new MainWebChromeClient(getContext()));
        webView.addJavascriptInterface(new WebAppInterface(), "android");
        com.speed_trap.android.WebAppInterface webAppInterface = new com.speed_trap.android.WebAppInterface(webView);
        webView.addJavascriptInterface(webAppInterface, webAppInterface.getAppBridgeJsName());

//        CelebrusCSAUtil.sessionSharing(this);
    }

    private class MainWebViewClient extends WebViewClient {

        private Context context;

        public MainWebViewClient(Context context) {
            this.context = context;
        }

        /**
         * @param view
         * @param request
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //Timber.d("shouldOverrideUrlLoading:"+request.getUrl().toString());
            if (request.getUrl().equals(environmentConfig.getWebUrl()) && !view.getTitle().equals("Teamwalk")) {
                webView.clearCache(true);
            }
            if (request.getUrl().toString().startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            } else {

                Uri cssoURL = Uri.parse(environmentConfig.getCssoUrl());
                if (TextUtils.equals(cssoURL.getHost(), request.getUrl().getHost())) {
                    if (TextUtils.equals("/csso/mobileIndex", request.getUrl().getPath())) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + env + "://login"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        return true;
                    }

                    return false;
                }

                Uri tcavURL = Uri.parse(environmentConfig.getTcavUrl());
                if (TextUtils.equals(tcavURL.getHost(), request.getUrl().getHost())) {
                    return false;
                }

                Uri webURL = Uri.parse(environmentConfig.getWebUrl());
                if (TextUtils.equals(webURL.getHost(), request.getUrl().getHost())) {
                    return false;
                }

                Uri taiwanlife_member_uri = Uri.parse(environmentConfig.getTaiwanlifeMemberUrl());
                if (TextUtils.equals(taiwanlife_member_uri.getHost(), request.getUrl().getHost())) {
                    String taiwanlife_member_str = environmentConfig.getTaiwanlifeMemberUrl();
                    String request_str = request.getUrl().toString();
                    try {
                        taiwanlife_member_str = URLDecoder.decode(environmentConfig.getTaiwanlifeMemberUrl());
                        request_str = URLDecoder.decode(request.getUrl().toString());
                    } catch (Exception e) {
                        taiwanlife_member_str = environmentConfig.getTaiwanlifeMemberUrl();
                        request_str = request.getUrl().toString();
                    }
                    if (TextUtils.equals(taiwanlife_member_str, request_str)) {
                        webView.loadUrl(environmentConfig.getWebUrl() + "my/preferences");
                        Timber.i("shouldOverrideUrlLoading: return my/preferences for www.taiwanlife.com");
                        mainWebViewJavaCallback.showPwErrorDialog();
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
            mainWebViewJavaCallback.enableLoading(true);
//            loadingIndicator.setVisibility(View.VISIBLE);
            if (url.startsWith(environmentConfig.getCssoUrl())) {
                mainWebViewJavaCallback.enableLoadingText(true, Color.BLACK);
//                loadingText.setVisibility(View.VISIBLE);
//                loadingText.setTextColor(Color.BLACK);
            } else if (url.startsWith("teamwalk")) {
                Uri parse = Uri.parse(url);
                String host = parse.getHost();
                String actionUrl = "";
                String suffic = "teamwalk" + env;
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
                    intent.putExtra("pid", mainWebViewJavaCallback.getPid());
                    view.stopLoading();
                    context.startActivity(intent);
                    if (url.contains("loginsuccess")) {
                        webView.loadUrl("about:blank");
                    }
                } else {
                    mainWebViewJavaCallback.toLogin();
                }
            } else if (url.equals(environmentConfig.getWebUrl())) {
//                loadingText.setVisibility(View.VISIBLE);
//                loadingText.setTextColor(Color.GRAY);
                mainWebViewJavaCallback.enableLoadingText(true, Color.GRAY);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Timber.d("Current URL = " + url);
            if (view.getUrl().equals(environmentConfig.getWebUrl()) && !view.getTitle().equals("Teamwalk")) {
//                clearCache = true;
                mainWebViewJavaCallback.setClearCache(true);
            }
            String csso_url = environmentConfig.getCssoUrl();
            String web_url = environmentConfig.getWebUrl();

            if (csso_url.length() > 0 && web_url.length() > 0) {
                String cookies = CookieManager.getInstance().getCookie(csso_url + "login");
                if (!TextUtils.isEmpty(cookies)) {
                    String[] cookieStringArray = cookies.split(";");
                    String CASTGC_value = null;
                    for (String cookie : cookieStringArray) {
                        if (cookie.contains("CASTGC")) {
                            CASTGC_value = "";
                            String[] cookieArray = cookie.split("=");
                            if (cookieArray.length < 2)
                                continue;
                            String cookieValue = cookieArray[1];
                            CASTGC_value = cookieValue;
                            SecuredPreferenceStoreManager.INSTANCE.simpleEditAndApply(Config.PREF_LOGIN_CASTGC, cookieValue);
                            break;
                        }
                    }

                    //Log.i("Cookies:", "onPageFinished setCookie:url="+url);
                    String loginsuccessStr = "teamwalkuat://loginsuccess";
                    if (BuildConfig.BUILD_TYPE.compareToIgnoreCase("release") == 0)
                        loginsuccessStr = "teamwalk://loginsuccess";
                    if (url.indexOf(loginsuccessStr) == 0 && CASTGC_value != null) {
                        String newCookies = "CASTGC=" + CASTGC_value;
                        CookieManager.getInstance().setCookie(web_url, newCookies);
                        CookieManager.getInstance().flush();
                        //Log.i("Cookies:", "onPageFinished setCookie:cookies=" + cookies);
                        //Log.i("Cookies:", "onPageFinished loginsuccess setCookie:newCookies=" + newCookies);
                    } else {
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

            mainWebViewJavaCallback.enableLoading(false);
            mainWebViewJavaCallback.enableLoadingText(false, Color.BLACK);
//            loadingIndicator.setVisibility(View.GONE);
//            loadingText.setVisibility(View.GONE);
        }

//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            super.onReceivedSslError(view, handler, error);
//            if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("sit")) {
//                handler.proceed();
//            }
//        }
    }

    private class MainWebChromeClient extends WebChromeClient {

        private Context context;

        public MainWebChromeClient(Context context) {
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
//            if (filePathCallback != null) {
//                filePathCallback.onReceiveValue(null);
//            }
//            filePathCallback = filePathCallbackParam;

            // 準備選項清單
            String[] selectOptions = new String[]{"選擇圖片", "拍照", "檔案"};
            mainWebViewJavaCallback.showBottomSheetDialog(new WebViewFileWrapper(filePathCallbackParam, fileChooserParams)); // 呼叫下方自訂的方法
            return true;  // 已自行處理檔案選擇介面
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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

            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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

    private class WebAppInterface {

        @JavascriptInterface
        public String signInUser() {
            String token = SecuredPreferenceStoreManager.INSTANCE.getString(Config.PREF_LOGIN_TOKEN, "");
            String refreshToken = SecuredPreferenceStoreManager.INSTANCE.getString(Config.PREF_LOGIN_REFRESH_TOKEN, "");
            Long exp = SecuredPreferenceStoreManager.INSTANCE.getLong(Config.PREF_LOGIN_EXP, 0L);
            String castgc = SecuredPreferenceStoreManager.INSTANCE.getString(Config.PREF_LOGIN_CASTGC, "");
            String uUid = SecuredPreferenceStoreManager.INSTANCE.getString(Config.PREF_LOGIN_UUID, "");
//            String token = loginSharedPref.getString(getString(R.string.pref_login_token), "");
//            String refreshToken = loginSharedPref.getString(getString(R.string.pref_login_refresh_token), "");
//            Long exp = loginSharedPref.getLong(getString(R.string.pref_login_exp), 0L);
//            String castgc = loginSharedPref.getString(getString(R.string.pref_login_castgc), "");
//            String uUid = loginSharedPref.getString(getString(R.string.pref_login_uuid), "");
            if (uUid == null || uUid.equals("")) {
//                uUid = fid;
                uUid = mainWebViewJavaCallback.getFid();
                SecuredPreferenceStoreManager.INSTANCE.simpleEditAndApply(Config.PREF_LOGIN_UUID, uUid);
//                loginSharedPref.edit().putString(getString(R.string.pref_login_uuid), uUid);
            }
            Map<String, String> user = new HashMap<String, String>();
//            user.put("pid", pid);
            user.put("pid", mainWebViewJavaCallback.getPid());
//            user.put("ticket", ticket);
            user.put("ticket", mainWebViewJavaCallback.getTicket());
            user.put("token", token);
            user.put("refreshToken", refreshToken);
            user.put("exp", String.valueOf(exp));
            user.put("castgc", castgc);
            user.put("fcm", fcmToken);
            user.put("fid", uUid);
            try {
                PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                user.put("vNo", pInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Timber.d("Fail to get package version");
            }
            return (new Gson()).toJson(user);
        }

        @JavascriptInterface
        public void copyToClipboard(String copyText) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("simple text", copyText);
            clipboard.setPrimaryClip(clip);

//            Toast.makeText(MainActivity.this, R.string.main_add_to_clipboard, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void bindGoogleFit() {
            // 版本29之後才需要要求此權限 29之前的可以直接執行
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ArrayList<String> permissions = new ArrayList<>();
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
                mainWebViewJavaCallback.getPermissionAndCallback(permissions, () -> {
                    mainWebViewJavaCallback.accessGoogleFit();
                });
            } else {
                mainWebViewJavaCallback.accessGoogleFit();
            }
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
//                    mainWebViewJavaCallback.accessGoogleFit();
//                } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)) {
//                } else {
//                    ActivityCompat.requestPermissions(getContext(),
//                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
//                            PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
//                }
//            } else {
//                mainWebViewJavaCallback.accessGoogleFit();
//            }
        }

        @JavascriptInterface
        public void bindFitbit() {
            String url = "https://www.fitbit.com/oauth2/authorize?" +
                    "client_id=" + environmentConfig.getConnectFitbitClientId() + "&" +
                    "response_type=code" + "&" +
                    "scope=" + "activity%20sleep" + "&" +
                    "expires_in=31536000&prompt=login%20consent&redirect_uri=teamwalk" + env + "://webconnect?device=fitbit";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getContext().startActivity(intent);
        }

        @JavascriptInterface
        public void bindGarmin() {
            mainWebViewJavaCallback.callGetGarminAuthCode();
//            String oauthConsumerKey = environmentConfig.getConnectGarminConsumerKey();
//            String oauthSignatureMethod = "HMAC-SHA1";
//
//            String oauthNonce = Utils.INSTANCE.randomString(10);
//
//            Date today = new Date();
//            long timestamp = today.getTime() / 1000L;
//            String oauthTimestamp = String.valueOf(timestamp);
//            String oauthVersion = "1.0";
//
//            String signature = "oauth_consumer_key=" + oauthConsumerKey + "&" +
//                    "oauth_nonce=" + oauthNonce + "&" +
//                    "oauth_signature_method=" + oauthSignatureMethod + "&" +
//                    "oauth_timestamp=" + oauthTimestamp + "&" +
//                    "oauth_version=" + oauthVersion;
//            String signatureBaseString = "";
//            try {
//                String signatureBase = URLEncoder.encode(Config.INSTANCE.GARMIN_BASE_URL + "request_token", "utf-8") + "&" + URLEncoder.encode(signature, "utf-8");
//                signatureBaseString = "POST&" + signatureBase;
//            } catch (UnsupportedEncodingException e) {
//                Timber.d("Fail to encode garmin url");
//            }
//
//            String keyString = environmentConfig.getConnectGarminConsumerSecret() + "&";
//            String oauthSignature = "";
//            try {
//                oauthSignature = URLEncoder.encode(Utils.INSTANCE.sha1(signatureBaseString, keyString), "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                Timber.d("Fail to encode fitbit url");
//            } catch (NoSuchAlgorithmException e) {
//                Timber.d("Fail to encode fitbit url");
//            } catch (InvalidKeyException e) {
//                Timber.d("Fail to encode fitbit url");
//            }
//
//            String authorization = "OAuth " + "oauth_version=\"" + oauthVersion + "\", " +
//                    "oauth_consumer_key=\"" + oauthConsumerKey + "\", " +
//                    "oauth_timestamp=\"" + oauthTimestamp + "\", " +
//                    "oauth_nonce=\"" + oauthNonce + "\", " +
//                    "oauth_signature_method=\"" + oauthSignatureMethod + "\", " +
//                    "oauth_signature=\"" + oauthSignature + "\"";
//
//            Toast connFailToast = Toast.makeText(getContext(), R.string.onboard_connect_fail, Toast.LENGTH_SHORT);
//
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl(Config.INSTANCE.GARMIN_BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            GarminTokenService garminTokenService = retrofit.create(GarminTokenService.class);
//            Call<ResponseBody> getAuthCodeCall = garminTokenService.getAuthCode(authorization);
//
//            getAuthCodeCall.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
////                    ResponseBody tokenString = response.body();
////                    String token = "";
////                    try {
////                        token = tokenString.string();
////                    } catch (IOException e) {
////                        Timber.d("Fail to get token");
////                    }
////
////                    String ts = token.substring(token.lastIndexOf("=") + 1, token.length());
////                    token = token.substring(0, token.indexOf("&"));
////
////                    if (TextUtils.isEmpty(token)) {
////                        connFailToast.show();
////                    } else {
////                        String url = "https://connect.garmin.com/oauthConfirm?" + token + "&" +
////                                "oauth_callback=teamwalk" + getString(R.string.env) + "://webconnect?device=garmin@" + ts;
////
////                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
////                        startActivity(intent);
////                    }
//
//                    try {
//                        if (response.body() != null) {
//                            String responseString = response.body().string();
//                            tsGarmin = responseString.substring(responseString.lastIndexOf("=") + 1, responseString.length());
//                            responseString = responseString.substring(0, responseString.indexOf("&"));
//                            if (TextUtils.isEmpty(responseString))
//                                connFailToast.show();
//                            else {
////                                String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
////                                        "oauth_callback=teamwalk" + getString(R.string.env) + "://webconnect?device=garmin@" + ts;
//                                String url = "https://connect.garmin.com/oauthConfirm?" + responseString + "&" +
//                                        "oauth_callback=teamwalk" + env + "://webconnectgarmin";
//
//                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                                getContext().startActivity(intent);
//                            }
//                        } else
//                            connFailToast.show();
//                    } catch (Exception e) {
//                        connFailToast.show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    connFailToast.show();
//                }
//            });
        }

        @JavascriptInterface
        public void removePattern() {
            SecuredPreferenceStoreManager.INSTANCE.editAndApply(editor -> {
                editor.putBoolean(Config.PREF_LOGIN_PATTERN_STATUS, false);
                editor.putInt(Config.PREF_LOGIN_SEGMENT_CONTROL_POS, 0);
                return Unit.INSTANCE;
            });
        }

        @JavascriptInterface
        public void removeGoogleFit() {
            // TODO
            mainWebViewJavaCallback.removeGoogleFit();
//            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                    .requestEmail()
//                    .build();
//
//            googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
//            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN_FOR_DISABLE_FIT);
        }

        @JavascriptInterface
        public void signOut() {
            SecuredPreferenceStoreManager.INSTANCE.editAndApply(editor -> {
                editor.putBoolean(Config.PREF_LOGIN_AUTH, false);
                editor.putString(Config.PREF_LOGIN_USERNAME, "");
                editor.putString(Config.PREF_LOGIN_TICKET, "");
                editor.putString(Config.PREF_LOGIN_CASTGC, "");
                editor.putString(Config.PREF_LOGIN_TOKEN, "");
                editor.putString(Config.PREF_LOGIN_REFRESH_TOKEN, "");
                editor.putLong(Config.PREF_LOGIN_EXP, 0L);
                return Unit.INSTANCE;
            });

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + env + "://login"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }

        @JavascriptInterface
        public void syncGoogleFit(String token) {
            // TODO
//            runOnUiThread(() -> {
//                teamwalkToken = token;
//                Calendar start = Calendar.getInstance();
//                start.add(Calendar.DATE, -6);
//                start.set(Calendar.HOUR_OF_DAY, 0);
//                start.set(Calendar.MINUTE, 0);
//                start.set(Calendar.SECOND, 0);
//                Calendar end = Calendar.getInstance();
//                end.add(Calendar.DATE, 1);
//                end.set(Calendar.HOUR_OF_DAY, 0);
//                end.set(Calendar.MINUTE, 0);
//                end.set(Calendar.SECOND, 0);
//                stepDataMap = new HashMap<>();
//                caloriesDataMap = new HashMap<>();
//                sleepDataList = new ArrayList<>();
//                getStepData(start.getTimeInMillis(), end.getTimeInMillis());
//            });
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
            return mainWebViewJavaCallback.shareContent(shareJsonStr);
        }

        @JavascriptInterface
        public boolean runScoreGooglePlay() {
            return mainWebViewJavaCallback.scoreGooglePlay();
        }

        @JavascriptInterface
        public void reloadPage() {
//            clearCache = true;
            mainWebViewJavaCallback.setClearCache(true);
        }
    }
}
