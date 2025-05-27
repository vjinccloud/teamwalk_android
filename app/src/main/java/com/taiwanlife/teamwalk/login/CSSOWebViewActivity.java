/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.speed_trap.android.WebAppInterface;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.util.DeviceUtil;
import com.taiwanlife.teamwalk.util.SensitiveDataUtil;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

/**
 * @author Vincent.Chen
 * @version 1
 * @date 2020/10/20
 */
public class CSSOWebViewActivity extends AppCompatActivity {

    private static final String TAG = "CSSOWebViewActivity";

    public static final String CSSO_SIGN_UP = "signup";

    private WebView webView;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceUtil.setFlagSecure(this);

        getWindow().setStatusBarColor(this.getColor(R.color.colorCTBCPrimary));

        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        webSettings.setBuiltInZoomControls(false);

        // Hide the zoom controls for HONEYCOMB+
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new CSSOWebViewClient(this));
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog alertDialog = new AlertDialog.Builder(CSSOWebViewActivity.this)
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

                AlertDialog alertDialog = new AlertDialog.Builder(CSSOWebViewActivity.this)
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
        });

//        SharedPreferences loginSharedPref = getSharedPreferences(getString(R.string.pref_login), CSSOWebViewActivity.MODE_PRIVATE);
        SecuredPreferenceStore loginSharedPref = SecuredPreferenceStore.getSharedInstance();
        String csso = loginSharedPref.getString("csso", CSSO_SIGN_UP);

        String cssoURL = getString(R.string.csso_sign_up_url);
        if (TextUtils.equals(CSSO_SIGN_UP, csso)) {
            cssoURL = getString(R.string.csso_sign_up_url);
        }

        if (TextUtils.equals(getString(R.string.csso_forget_pwd_key), csso)) {
            cssoURL = getString(R.string.csso_forget_mima_url);
        }

        webView.addJavascriptInterface(new WebAppInterface(webView), new WebAppInterface(webView).getAppBridgeJsName());

//        CelebrusCSAUtil.sessionSharing(this);

        webView.loadUrl(cssoURL);
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void backupSensitiveData() {
    }

    private void restoreSensitiveData() {
    }

    private void clearSensitiveData(boolean isDestroy) {
        if (isDestroy) {
            webView.loadUrl("about:blank");
            SensitiveDataUtil.clearWebViewSensitiveData(this, webView, isDestroy);
        }
    }

    /**
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private class CSSOWebViewClient extends WebViewClient {

        private Context context;

        public CSSOWebViewClient(Context context) {
            this.context = context;
        }

        /**
         * @param view
         * @param request
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            Log.d(TAG, request.getUrl().toString());

            Uri cssoURL = Uri.parse(getString(R.string.csso_url));
            if (TextUtils.equals(cssoURL.getHost(), request.getUrl().getHost())) {
                if (TextUtils.equals("/csso/mobileIndex", request.getUrl().getPath())) {
                    finish();
                    return true;
                }

                return false;
            }

            if (request.getUrl().getHost().contains("bid.g.doubleclick.net")) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
    }
}