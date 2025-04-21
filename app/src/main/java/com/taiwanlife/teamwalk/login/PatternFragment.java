/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.model.CSSOQueryUserBody;
import com.taiwanlife.teamwalk.model.CSSOUser;
import com.taiwanlife.teamwalk.model.PatternDisableBody;
import com.taiwanlife.teamwalk.service.CSSOQueryUserService;
import com.taiwanlife.teamwalk.service.PatternService;
import com.taiwanlife.teamwalk.util.AbstractTextValidator;
import com.taiwanlife.teamwalk.util.Utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.taiwanlife.teamwalk.login.CSSOWebViewActivity.CSSO_SIGN_UP;
import static com.taiwanlife.teamwalk.util.PIDTextInputUtil.chkPIDFormat;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class PatternFragment extends Fragment implements LoginMethod {

    private static final String TAG = "PatternFragment";
    private static final String APP = "Teamwalk";

    private Login loginContext;

    private SecuredPreferenceStore sharedPref;

    private boolean isRememberMe;
    private String pid;
    private EditText pidEditText;
    View pidLayout;
    TextView pidExistWarning;

    private PatternLockView patternLockView;
    private boolean inStealthMode = false;
    private Activity activity;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        sharedPref = getActivity().getSecuredPreferenceStore(getString(R.string.pref_login), LoginActivity.MODE_PRIVATE);
        sharedPref = SecuredPreferenceStore.getSharedInstance();
        isRememberMe = sharedPref.getBoolean(getString(R.string.pref_login_remember_me), false);
        pid = isRememberMe ? sharedPref.getString(getString(R.string.pref_login_pid), "") : "";
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_pattern, container, false);

        setPID(view);
        setRememberMe(view);
        setPatternLock(view);
        setToggleInStealthMode(view);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(pid)) {
            pidEditText.setText(pid.substring(0, 3) + "*****" + pid.substring(8, 10));
        }
    }

    /**
     *
     * @param loginContext
     */
    @Override
    public void setOnFragmentAttachedListener(Login loginContext) {
        this.loginContext = loginContext;
    }

    /**
     *
     * @param pid
     * @param intent
     */
    @Override
    public void queryUser(String pid, Intent intent) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.csso_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CSSOQueryUserService queryUserService = retrofit.create(CSSOQueryUserService.class);
        Call<CSSOUser> queryUserCall = queryUserService.queryUser(new CSSOQueryUserBody(pid));

        String prefPatternStatus = getString(R.string.pref_login_pattern_status);

        String notSetTitle = getActivity().getString(R.string.login_alert_pattern_not_set_title);
        String notSetBody = getActivity().getString(R.string.login_alert_pattern_not_set_body);
        String cancelBtnText = getActivity().getString(R.string.cancel);
        String overYearTitle = getActivity().getString(R.string.login_alert_pattern_over_year_title);
        String overYearBody = getActivity().getString(R.string.login_alert_pattern_over_year_body);
        String unAuthTitle = getActivity().getString(R.string.login_alert_user_unauthorized_title);
        String unAuthBody = getActivity().getString(R.string.login_alert_user_unauthorized_body);
        String notFoundTitle = getActivity().getString(R.string.login_alert_user_not_found_title);
        String notFoundBody = getActivity().getString(R.string.login_alert_user_not_found_body);
        String registerBtnText = getActivity().getString(R.string.register_now);
        String registerNext = getActivity().getString(R.string.register_next);

        SecuredPreferenceStore.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("csso", CSSO_SIGN_UP);
        prefEditor.apply();

        Intent signupIntent = new Intent(getActivity(), CSSOWebViewActivity.class);

        Toast connFailToast = Toast.makeText(getActivity(), getActivity().getString(R.string.access_connect_fail), Toast.LENGTH_LONG);

        queryUserCall.enqueue(new Callback<CSSOUser>() {
            @Override
            public void onResponse(Call<CSSOUser> call, Response<CSSOUser> response) {
                CSSOUser cssoUser = response.body();

                if (cssoUser == null) {
                    Log.w(TAG, "Fail to csso query user");
                    connFailToast.show();
                } else {
                    String rspCode = cssoUser.getRspCode();

                    DialogFragment dialogFragment;

                    if (!TextUtils.isEmpty(rspCode) && rspCode.equals(CSSOQueryUserService.QUERY_USER_RSP_CODE_SUCCESS)) {
                        Log.i(TAG, "query user found");

                        SecuredPreferenceStore.Editor editor = sharedPref.edit();

                        String patternLockStatus = cssoUser.getPatternLockStatus();
                        Log.d(TAG, "pattern status enable: "+ sharedPref.getBoolean(prefPatternStatus, false));
                        switch (patternLockStatus) {
                            case "Y":
                                Log.d(TAG, "pattern lock status: Y");
                                patternLockView.setInputEnabled(true);
                                editor.putBoolean(prefPatternStatus, true);
//                                if (fn != null) fn.apply(null);
                                if (intent != null) {
                                    activity.setResult(Activity.RESULT_OK, intent);;
                                    activity.finish();
                                }
                                break;

                            case "N":
                                Log.d(TAG, "pattern lock status: N");
                                patternLockView.setInputEnabled(false);
                                editor.putBoolean(prefPatternStatus, false);
                                dialogFragment = loginContext.buildAlert(
                                        notSetTitle,
                                        notSetBody,
                                        R.drawable.alert_1,
                                        cancelBtnText,
                                        null,
                                        null,
                                        false,
                                        0);
                                loginContext.showAlert(dialogFragment);
                                break;

                            case "E":
                                Log.d(TAG, "pattern lock status: E");
                                patternLockView.setInputEnabled(false);
                                if (sharedPref.getBoolean(prefPatternStatus, false)) {
                                    disablePattern(pid);
                                }
                                editor.putBoolean(prefPatternStatus, false);
                                dialogFragment = loginContext.buildAlert(
                                        notSetTitle,
                                        notSetBody,
                                        R.drawable.alert_1,
                                        cancelBtnText,
                                        null,
                                        null,
                                        false,
                                        0);
                                loginContext.showAlert(dialogFragment);
                                break;

                            case "O":
                                Log.d(TAG, "pattern lock status: O");
                                patternLockView.setInputEnabled(true);
                                editor.putBoolean(prefPatternStatus, true);
                                dialogFragment = loginContext.buildAlert(
                                        overYearTitle,
                                        overYearBody,
                                        R.drawable.alert_1,
                                        cancelBtnText,
                                        null,
                                        null,
                                        false,
                                        0);
                                loginContext.showAlert(dialogFragment);
                                break;
                        }

                        editor.apply();
                    }

                    if (!TextUtils.isEmpty(rspCode) && rspCode.equals(CSSOQueryUserService.QUERY_USER_RSP_CODE_UNAUTHORIZED)) {
                        Log.i(TAG, "query user unauthorized");

                        patternLockView.setInputEnabled(false);

                        dialogFragment = loginContext.buildAlert(
                                unAuthTitle,
                                unAuthBody,
                                R.drawable.alert_1,
                                cancelBtnText,
                                null,
                                null,
                                false,
                                0);
                        loginContext.showAlert(dialogFragment);
                    }

                    if (!TextUtils.isEmpty(rspCode) && rspCode.equals(CSSOQueryUserService.QUERY_USER_RSP_CODE_USER_NOT_FOUND)) {
                        Log.i(TAG, "query user not found");
                        dialogFragment = loginContext.buildAlert(
                                notFoundTitle,
                                notFoundBody,
                                R.drawable.alert_1,
                                registerNext,
                                registerBtnText,
                                signupIntent,
                                false,

                                 0);
                        loginContext.showAlert(dialogFragment);
                    }
                }
            }

            @Override
            public void onFailure(Call<CSSOUser> call, Throwable t) {
                connFailToast.show();
            }
        });
    }

    /**
     *
     * @param v
     */
    private void setRememberMe(View v) {
        CheckBox checkBox = v.findViewById(R.id.login_checkBox_remember_me);
        checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            isRememberMe = isChecked;

            SecuredPreferenceStore.Editor editor = sharedPref.edit();
            if (isChecked) {
                editor.putBoolean(getString(R.string.pref_login_remember_me), true);
                editor.putString(getString((R.string.pref_login_pid)), pid);
            } else {
                editor.putBoolean(getString(R.string.pref_login_remember_me), false);
                editor.putString(getString((R.string.pref_login_pid)), "");
            }
            editor.apply();
        });

        checkBox.setChecked(isRememberMe);
    }

    /**
     *
     * @param v
     */
    private void setPID(View v) {
        pidLayout = v.findViewById(R.id.login_layout_pattern_pid);
        pidExistWarning = v.findViewById(R.id.login_textView_warning_pid);
        pidEditText = v.findViewById(R.id.login_editText_pattern_pid);
        pidEditText.addTextChangedListener(new AbstractTextValidator(pidEditText) {
            @Override
            public void validate(TextView textView, String text) {
//                Log.d(TAG, text);
                if (text.length() != 10) {
                    return;
                }
                if (pidExistWarning.getVisibility() == View.VISIBLE) {
                    pidLayout.setBackground(null);
                    pidExistWarning.setVisibility(View.GONE);
                }

                if (TextUtils.isEmpty(text) || text.length() != 10) return;

                if (text.contains("*****")) {
                    text = pid;
                }

//                boolean wrongPID = !chkPIDFormat(text);
//
//                if (wrongPID) {
//                    pidLayout.setBackgroundColor(getActivity().getColor(R.color.colorError));
//                    pidExistWarning.setVisibility(View.VISIBLE);
//                } else {
//
//                }
                pid = text.toUpperCase();

                queryUser(pid, null);

                if (isRememberMe) {
                    SecuredPreferenceStore.Editor editor = sharedPref.edit();
                    editor.putString(getString((R.string.pref_login_pid)), pid);
                    editor.apply();
                }
            }
        });
    }

    /**
     *
     * @param v
     */
    private void setPatternLock(View v) {
        patternLockView = v.findViewById(R.id.login_pattern_lock_view);
        patternLockView.setInStealthMode(inStealthMode);
        patternLockView.setInputEnabled(false);
        Log.d(TAG, "pattern enable: false");
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) { Log.d(TAG, "Pattern in progress"); }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                String fid = sharedPref.getString(getString(R.string.pref_login_fid), "");

//                Log.d(TAG, "Pattern complete: " +
//                        Utilities.patternToSha256(patternLockView, pattern, fid));

                if (TextUtils.isEmpty(pidEditText.getText().toString()) || pidEditText.getText().toString().length() != 10) {
                    pidLayout.setBackgroundColor(getActivity().getColor(R.color.colorError));
                    pidExistWarning.setVisibility(View.VISIBLE);
                    return;
                }

                if (pattern.size() < 6) {
                    Toast.makeText(getActivity(), R.string.login_pattern_lt_six_dots, Toast.LENGTH_LONG).show();
                } else if (pattern.size() > 16) {
                    Toast.makeText(getActivity(), R.string.login_pattern_bt_dots, Toast.LENGTH_LONG).show();
                } else {
                    Set<Integer> dotList = new HashSet<Integer>();
                    for (PatternLockView.Dot dot : pattern) {
                        dotList.add(dot.getId());
                    }

                    if (dotList.size() < 6) {
                        Toast.makeText(getActivity(), R.string.login_pattern_lt_six_dots, Toast.LENGTH_LONG).show();
                    } else {

                        String loginURL = getActivity().getString(R.string.csso_url) + "patternLogin";
                        String loginParams = "SYS_ID=teamwalk" + "&" +
                                "userId=" + pid + "&" +
                                "pattern_path=" + Utilities.patternToSha256(patternLockView, pattern, fid) + "&" +
                                "service=teamwalk" + getActivity().getString(R.string.env) + "://loginsuccess";
                        Intent signInIntent = new Intent();
                        signInIntent.putExtra("pid", pid);
                        signInIntent.putExtra("url", loginURL);
                        signInIntent.putExtra("params", loginParams);
//                        signInIntent.putExtra("pattern_path", Utilities.patternToSha256(patternLockView, pattern, fid));
//                        signInIntent.putExtra("isPattern", true);

                        queryUser(pid, signInIntent);
                    }
                }
            }

            @Override
            public void onCleared() {
                Log.d(TAG, "Pattern has been cleared");
            }
        });
    }

    /**
     *
     * @param v
     */
    private void setToggleInStealthMode(View v) {
        Button toggleButton = v.findViewById(R.id.login_pattern_toggle_stealth_mode_button);
        toggleButton.setOnClickListener((View view) -> {
            patternLockView.setInStealthMode(inStealthMode ? false : true);
            toggleButton.setText(inStealthMode ? getActivity().getString(R.string.login_pattern_normal_mode) : getActivity().getString(R.string.login_pattern_stealth_mode));
            if (inStealthMode) {
                toggleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility, 0);
            } else {
                toggleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0);
            }

            inStealthMode = inStealthMode ? false : true;
            patternLockView.clearPattern();
        });
    }

    private void disablePattern(final String pid) {
        new Thread(() -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.api_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            PatternService patternService = retrofit.create(PatternService.class);
            Call<Map<String, Object>> call = patternService.disablePattern(new PatternDisableBody(pid));
            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response != null && response.body() != null) {
                        Map<String, Object> result = response.body();
                        double status = (double) result.get("status_code");
                        if ( (int)status == 200) {
                            Log.i(TAG, "pattern lock disable success");
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.i(TAG, "pattern lock disable failed");
                }
            });
        }).start();
    }
}