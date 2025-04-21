/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.util;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.google.gson.Gson;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.login.LoginActivity;
import com.taiwanlife.teamwalk.model.Pattern;
import com.taiwanlife.teamwalk.onboard.OnboardActivity;
import com.taiwanlife.teamwalk.service.PatternService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public class PatternSetupActivity extends OnboardActivity implements AlertDialogFragment.Builder {

    private static final String TAG = "PatternSetupActivity";

    private String castgc;
    private PatternLockView patternLockView;
    private TextView titleView;
    private TextView bodyView;

    private boolean drawOrigPattern = false;
    private String origPattern = "";
    private boolean drawNewPattern = true;
    private String newPattern;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_setup);

//        SecuredPreferenceStore sharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), PatternSetupActivity.MODE_PRIVATE);
        SecuredPreferenceStore sharedPref = SecuredPreferenceStore.getSharedInstance();

        Uri uri = getIntent().getData();
        if (uri != null) {

            String scheme_action = uri.getHost();

            if (TextUtils.equals(scheme_action, "pattern_setup")) {

                String username = uri.getQueryParameter("username");
                String ticket = uri.getQueryParameter("ticket");
                String token = uri.getQueryParameter("token");
                String refreshToken =  uri.getQueryParameter("refreshToken");
                Long exp = Long.valueOf(uri.getQueryParameter("exp"));

                storeUser(username, ticket, token, refreshToken, exp);
            }

            castgc = sharedPref.getString(getString(R.string.pref_login_castgc), "");
        }

        titleView = findViewById(R.id.pattern_setup_title);
        bodyView = findViewById(R.id.pattern_setup_body);

        setBack();
        setPatternLockView();

//        AlertDialogPattern dialogFragment = createDialog(getString(R.string.pattern_setup_notice_title), getString(R.string.pattern_setup_notice_content), R.drawable.alert_2, getString(R.string.close));
//        dialogFragment.show(getSupportFragmentManager(), TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUser();
        prepareSetup();
    }

    private void prepareSetup() {
//        SecuredPreferenceStore sharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), PatternSetupActivity.MODE_PRIVATE);
        SecuredPreferenceStore sharedPref = SecuredPreferenceStore.getSharedInstance();

        if (TextUtils.isEmpty(getUname()) || TextUtils.isEmpty(castgc)) {
            patternLockView.setInputEnabled(false);
            Toast.makeText(PatternSetupActivity.this, R.string.access_data_fail, Toast.LENGTH_LONG).show();
        }

        drawOrigPattern = sharedPref.getBoolean(getString(R.string.pref_login_pattern_status), false);
        if (drawOrigPattern) {
            titleView.setText(getString(R.string.pattern_setup_original_title));
            bodyView.setText(R.string.pattern_setup_original_body);
        }

        drawNewPattern = true;
    }

    private void setBack() {
        ImageButton button = findViewById(R.id.pattern_setup_back);
        button.setOnClickListener((View v) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://userinfo"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setPatternLockView() {
        patternLockView = findViewById(R.id.pattern_lock_view_setup);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
                Log.d(TAG, "Pattern in progress");
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                Log.d(TAG, "Pattern complete");

                if (pattern.size() < 6) {
                    Toast.makeText(PatternSetupActivity.this, R.string.login_pattern_lt_six_dots, Toast.LENGTH_LONG).show();
                } else if (pattern.size() > 14) {
                    Toast.makeText(PatternSetupActivity.this, R.string.login_pattern_bt_dots, Toast.LENGTH_LONG).show();
                } else {
                    Set<Integer> dotList = new HashSet<Integer>();
                    for (PatternLockView.Dot dot : pattern) {
                        dotList.add(dot.getId());
                    }

                    if (dotList.size() < 6) {
                        Toast.makeText(PatternSetupActivity.this, R.string.login_pattern_lt_six_dots, Toast.LENGTH_LONG).show();
                    } else {
                        processSetup(pattern);
                    }
                }
            }

            @Override
            public void onCleared() {
                Log.d(TAG, "Pattern cleared");
            }
        });
    }

    /**
     *
     * @param pattern
     */
    private void processSetup(List<PatternLockView.Dot> pattern) {
        TextView titleView = findViewById(R.id.pattern_setup_title);
        TextView bodyView = findViewById(R.id.pattern_setup_body);

//        SecuredPreferenceStore sharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), PatternSetupActivity.MODE_PRIVATE);
        SecuredPreferenceStore sharedPref = SecuredPreferenceStore.getSharedInstance();
        String fid = sharedPref.getString(getString(R.string.pref_login_fid), "");

        if (drawOrigPattern) {
            origPattern = Utilities.patternToSha256(patternLockView, pattern, fid);
            drawOrigPattern = false;

            titleView.setText(getString(R.string.pattern_setup_title));
            bodyView.setText(R.string.pattern_setup_body);

            patternLockView.clearPattern();

            return;
        }

        if (drawNewPattern) {
            newPattern = Utilities.patternToSha256(patternLockView, pattern, fid);
            drawNewPattern = false;

            titleView.setText(getString(R.string.pattern_setup_confirm_title));
            bodyView.setText(R.string.pattern_setup_confirm_body);

            patternLockView.clearPattern();
        } else {
            String confirmPattern = Utilities.patternToSha256(patternLockView, pattern, fid);

            if (TextUtils.equals(newPattern, confirmPattern)) {
                // setup success
                String url = getString(R.string.csso_url) + "rest/setPatternLock";
//                new PatternSetupActivity.PostToCSSOTask().execute(url, uname, castgc, origPattern, newPattern);
                setPattern(castgc, getUname(), origPattern, newPattern);
            } else {
//                Toast.makeText(PatternSetupActivity.this, R.string.setup_fail, Toast.LENGTH_LONG).show();
                showAlertFailed();
            }
        }
    }

    /**
     *
     * @param title
     * @param msg
     * @param image
     * @param buttonText
     * @return
     */
    @Override
    public AlertDialogFragment createDialog(String title, String msg, int image, String buttonText) {
        return AlertDialogFragment.newInstance(title, msg, image, buttonText);
    }

    /**
     *
     * @param title
     * @param msg
     * @param image
     * @param buttonText
     * @param positiveButtonText
     * @param positiveIntent
     * @return
     */
    @Override
    public AlertDialogFragment createDialogWithPositionBtn(String title, String msg, int image, String buttonText, String positiveButtonText, Intent positiveIntent, boolean forResult, int resultCode) {
        AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(title, msg, image, buttonText);
        dialogFragment.setPositiveButton(positiveButtonText, positiveIntent, forResult, resultCode);
        return dialogFragment;
    }

    @Override
    public AlertDialogFragment createDialogWithNegativeButton(String title, String msg, int image,String negativeText, Intent negativeIntent, boolean forResult, int resultCode) {
        AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(title, msg, image, negativeText);
        dialogFragment.setNegativeButton(negativeIntent, forResult, resultCode);
        return dialogFragment;
    }

    private class PostToCSSOTask extends AsyncTask<String, Void, Boolean> {

        /**
         *
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(String... params) {

            InputStream stream = null;
            HttpsURLConnection connection = null;
            String bodyInputString = params[3].length() > 0 ?
                    "{ 'userId': '" + params[1] + "', 'newPatternLock': '" + params[4] + "', 'origPatternLock': '" + params[3] + "'}"
                    :
                    "{ 'userId': '" + params[1] + "', 'newPatternLock': '" + params[4] + "'}";
//            Log.d(TAG, bodyInputString);
            String result = null;
            OutputStream os = null;

            try {
                URL url = new URL((String) params[0]);
                connection = (HttpsURLConnection) url.openConnection();

                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Cookie", "CASTGC=" + params[2]);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                os = connection.getOutputStream();
                byte[] input = bodyInputString.getBytes("utf-8");
                os.write(input, 0, input.length);

                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                stream = connection.getInputStream();

                if (stream != null) {
                    result = readInputStreamToString(connection);
                }
            } catch (IOException e) {
//                Log.e(TAG, "csso login failed", e);
                Intent intent = new Intent(PatternSetupActivity.this, LoginActivity.class);
                startActivity(intent);
            } finally {
                if(os !=null){
                    try{
                        os.close();
                    }catch (Exception e){}
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.d(TAG, "Fail to close stream");
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            if (TextUtils.isEmpty(result)) {
                Looper.prepare();
//                Toast.makeText(PatternSetupActivity.this, R.string.setup_fail, Toast.LENGTH_LONG).show();
                showAlertFailed();
                Looper.loop();
            } else {
                Gson gson = new Gson();
                Map resultMap = gson.fromJson(result, Map.class);

                String respCode = (String) resultMap.get("rspCode");
                if (TextUtils.isEmpty(respCode)) {
                    Looper.prepare();
//                    Toast.makeText(PatternSetupActivity.this, R.string.setup_fail, Toast.LENGTH_LONG).show();
                    showAlertFailed();
                    Looper.loop();
                } else {

                    if (TextUtils.equals(respCode, "0000")) {
                        return true;
                    }

                    if (TextUtils.equals(respCode, "0401")) {
                        Looper.prepare();
                        Toast.makeText(PatternSetupActivity.this, R.string.pattern_setup_resp_401, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }
            }

            return false;
        }

        /**
         *
         * @param isCompleted
         */
        @Override
        protected void onPostExecute(Boolean isCompleted) {
            Intent backToPrefIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://userinfo"));
            backToPrefIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            if (isCompleted) {
                String msg = getString(R.string.change_success_text);
                if (getMyUser() != null) {
                    getMyUser().setLoginMethod("PATTERN");
                    updateUser(getMyUser(), true);
                }
                AlertDialogFragment dialogFragment = createDialogWithNegativeButton(getString(R.string.change_success), msg, R.drawable.alert_1, getString(R.string.close),  backToPrefIntent, false, 0);
                dialogFragment.setBodyTextAlign(View.TEXT_ALIGNMENT_TEXT_START);
                dialogFragment.show(getSupportFragmentManager(), TAG);

            } else {
                AlertDialogFragment dialogFragment = createDialogWithNegativeButton(getString(R.string.access_connect_fail), getString(R.string.setup_fail_msg), R.drawable.alert_3, getString(R.string.close), backToPrefIntent, false, 0);
                dialogFragment.show(getSupportFragmentManager(), TAG);
            }
        }

        /**
         *
         * @param connection
         * @return
         */
        private String readInputStreamToString(HttpsURLConnection connection) {
            String result = null;
            StringBuffer sb = new StringBuffer();
            InputStream is = null;

            try {
                is = new BufferedInputStream(connection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                result = sb.toString();
            }
            catch (IOException e) {
                Log.i(TAG, "Error reading InputStream");
                result = null;
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        Log.i(TAG, "Error closing InputStream");
                    }
                }
            }

            return result;
        }
    }

    private void setPattern(String castgc,String uname, String origPattern, String newPattern) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.csso_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PatternService patternService = retrofit.create(PatternService.class);
        Call<Map<String, String>> queryUserCall = patternService.setPatternLock("CASTGC=" + castgc, new Pattern(uname, newPattern, origPattern));

        queryUserCall.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Intent backToPrefIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://userinfo"));
                backToPrefIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (response != null && response.body() != null) {
                    Map<String, String> body = response.body();
                    String rspCode = body.get("rspCode");
                    if (TextUtils.equals(rspCode, "0000")) {
                        String msg = getString(R.string.change_success_text);
                        if (getMyUser() != null) {
                            getMyUser().setLoginMethod("PATTERN");
                            updateUser(getMyUser(), true);
                        }
                        AlertDialogFragment dialogFragment = createDialogWithNegativeButton(getString(R.string.change_success), msg, R.drawable.alert_2, getString(R.string.close), backToPrefIntent, false, 0);
                        dialogFragment.setBodyTextAlign(View.TEXT_ALIGNMENT_TEXT_START);
                        dialogFragment.show(getSupportFragmentManager(), TAG);
                    } else if (TextUtils.equals(rspCode, "0401")) {
                        AlertDialogFragment dialogFragment = createDialogWithNegativeButton(getString(R.string.access_connect_fail), getString(R.string.setup_fail_msg), R.drawable.alert_3, getString(R.string.close), backToPrefIntent, false, 0);
                        dialogFragment.show(getSupportFragmentManager(), TAG);
                    }

                } else {
                    showAlertFailed();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                showAlertFailed();
            }
        });
    }

    private void showAlertFailed() {
        Intent backToPrefIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + getString(R.string.env) + "://userinfo"));
        AlertDialogFragment dialogFragment = createDialogWithNegativeButton( getString(R.string.setup_fail),getString(R.string.setup_fail_msg),  R.drawable.alert_3, getString(R.string.close), backToPrefIntent, false, 0);
        dialogFragment.show(getSupportFragmentManager(), TAG);
        backToPrefIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}