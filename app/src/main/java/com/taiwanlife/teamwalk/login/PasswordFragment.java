/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.model.CSSOQueryUserBody;
import com.taiwanlife.teamwalk.model.CSSOUser;
import com.taiwanlife.teamwalk.service.CSSOQueryUserService;
import com.taiwanlife.teamwalk.util.AbstractTextValidator;
import com.taiwanlife.teamwalk.util.Utilities;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.taiwanlife.teamwalk.login.CSSOWebViewActivity.CSSO_SIGN_UP;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class PasswordFragment extends Fragment implements LoginMethod {

    private static final String TAG = "PasswordFragment";
    private static final String APP = "Teamwalk";

    private Login loginContext;

    private SecuredPreferenceStore sharedPref;

    private boolean isRememberMe;
    private String pid;
    private EditText pidEditText;
    View pidLayout;
    TextView pidExistWarning;

    private EditText passwordEditText;
    View passwordLayout;
    TextView passwordExistWarning;

    private EditText captchaEditText;
    private String genText;
    View captchaLayout;
    TextView captchaExistWarning;
    private Button captchaButton;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = SecuredPreferenceStore.getSharedInstance();
        isRememberMe = sharedPref.getBoolean(getString(R.string.pref_login_remember_me), false);
        pid = isRememberMe ? sharedPref.getString(getString(R.string.pref_login_pid), "") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_password, container, false);
        setForgetPassword(view);
        setSignup(view);

        setPID(view);
        setRememberMe(view);
        setPassword(view);
        setRandomNumbers(view);
        setRandomNumbersGen(view);
        setLogin(view);

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
            pidEditText.setText(pid.substring(0, 3) + "*****" + pid.substring(8));
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

        String unAuthTitle = getActivity().getString(R.string.login_alert_user_unauthorized_title);
        String unAuthBody = getActivity().getString(R.string.login_alert_user_unauthorized_body);
        String notFoundTitle = getActivity().getString(R.string.login_alert_user_not_found_title);
        String notFoundBody = getActivity().getString(R.string.login_alert_user_not_found_body);
        String cancelBtnText = getActivity().getString(R.string.cancel);
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
                Log.i("LOG TIME CSSO queryUser end: " , Utilities.getDateNow());
                CSSOUser cssoUser = response.body();

                if (cssoUser == null) {
                    Log.w(TAG, "Fail to csso query user");
                    connFailToast.show();
                } else {
                    String rspCode = cssoUser.getRspCode();

                    if (!TextUtils.isEmpty(rspCode) && rspCode.equals(CSSOQueryUserService.QUERY_USER_RSP_CODE_SUCCESS)) {
                        Log.i(TAG, "query user found");

                        SecuredPreferenceStore.Editor editor = sharedPref.edit();

                        String patternLockStatus = cssoUser.getPatternLockStatus();
                        if (TextUtils.equals(patternLockStatus, "Y") || TextUtils.equals(patternLockStatus, "O")) {
                            editor.putBoolean(prefPatternStatus, true);
                        }
                        if (TextUtils.equals(patternLockStatus, "N") || TextUtils.equals(patternLockStatus, "E")) {
                            editor.putBoolean(prefPatternStatus, false);
                        }
                        editor.apply();

                        if (intent != null) {
                            activity.setResult(Activity.RESULT_OK, intent);
                            activity.finish();
                            Log.i("LOG TIME to main: " , Utilities.getDateNow());
                        }
                    }

                    if (!TextUtils.isEmpty(rspCode) && rspCode.equals(CSSOQueryUserService.QUERY_USER_RSP_CODE_UNAUTHORIZED)) {
                        Log.i(TAG, "query user unauthorized");

                        passwordEditText.setEnabled(false);
                        passwordEditText.setInputType(InputType.TYPE_NULL);

                        captchaEditText.setEnabled(false);
                        captchaEditText.setInputType(InputType.TYPE_NULL);

                        DialogFragment dialogFragment = loginContext.buildAlert(
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
                        DialogFragment dialogFragment = loginContext.buildAlert(
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
    private void setForgetPassword(View v) {
        TextView forgetPass = v.findViewById(R.id.login_forget_pass_textView);
        String forgetPassString = getString(R.string.forget_pass);
        if (!TextUtils.isEmpty(forgetPassString)) {
            SpannableString ss = new SpannableString(forgetPassString);
            ClickableSpan cs = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(getActivity().getColor(R.color.colorLinkSecondaryText));
                }

                @Override
                public void onClick(@NonNull View widget) {
                    SecuredPreferenceStore.Editor prefEditor = sharedPref.edit();
                    prefEditor.putString("csso", getString(R.string.csso_forget_pwd_key));
                    prefEditor.apply();

                    activity.setResult(Activity.RESULT_CANCELED);
                    activity.finish();

                    Intent intent = new Intent(activity, CSSOWebViewActivity.class);
                    startActivity(intent);
                }
            };

            ss.setSpan(cs, 0, forgetPassString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            forgetPass.setText(ss);
            forgetPass.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    /**
     *
     * @param v
     */
    private void setSignup(View v) {
        TextView signUp = v.findViewById(R.id.login_signup_textView);
        String signUpString = getString(R.string.sign_up);
        if (!TextUtils.isEmpty(signUpString)) {
            SpannableString ss = new SpannableString(signUpString);
            ClickableSpan cs = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(getActivity().getColor(R.color.colorLinkText));
                }

                @Override
                public void onClick(@NonNull View widget) {
                    SecuredPreferenceStore.Editor prefEditor = sharedPref.edit();
                    prefEditor.putString("csso", CSSO_SIGN_UP);
                    prefEditor.apply();

                    activity.setResult(Activity.RESULT_CANCELED);
                    activity.finish();

                    Intent intent = new Intent(activity, CSSOWebViewActivity.class);
                    startActivity(intent);
                }
            };

            ss.setSpan(cs, 0, signUpString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            signUp.setText(ss);
            signUp.setMovementMethod(LinkMovementMethod.getInstance());
        }
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
        pidLayout = v.findViewById(R.id.login_layout_password_pid);
        pidExistWarning = v.findViewById(R.id.login_textView_warning_pid);
        pidEditText = v.findViewById(R.id.login_editText_password_pid);
//        pidEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pidEditText.addTextChangedListener(new AbstractTextValidator(pidEditText) {
            @Override
            public void validate(TextView textView, String text) {
//                Log.d(TAG, text);
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
    private void setPassword(View v) {
        passwordLayout = v.findViewById(R.id.login_layout_password_password);
        passwordExistWarning = v.findViewById(R.id.login_textView_warning_password);
        passwordEditText = v.findViewById(R.id.login_editText_password);
        passwordEditText.addTextChangedListener(new AbstractTextValidator(passwordEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (passwordExistWarning.getVisibility() == View.VISIBLE) {
                    passwordLayout.setBackground(null);
                    passwordExistWarning.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     *
     * @param v
     */
    private void setRandomNumbers(View v) {
        captchaLayout = v.findViewById(R.id.login_layout_password_captcha);
        captchaExistWarning = v.findViewById(R.id.login_textView_warning_captcha);
        captchaEditText = v.findViewById(R.id.login_editText_captcha);
        captchaEditText.addTextChangedListener(new AbstractTextValidator(captchaEditText) {

            @Override
            public void validate(TextView textView, String text) {
                if (captchaExistWarning.getVisibility() == View.VISIBLE) {
                    captchaLayout.setBackground(null);
                    captchaExistWarning.setVisibility(View.GONE);
                }
            }
        });
        captchaEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 执行登录操作
                    login();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     *
     * @param v
     */
    private void setRandomNumbersGen(View v) {
        captchaButton = v.findViewById(R.id.login_button_captcha);
        captchaButton.setOnClickListener((View view) -> {
            genText = genRandomNumbers();
            captchaButton.setText(genText);
        });

        genText = genRandomNumbers();
        captchaButton.setText(genText);
    }

    /**
     *
     * @return
     */
    private String genRandomNumbers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            SecureRandom secureRandom = new SecureRandom();
            int randomInt = secureRandom.nextInt((int)'Z' - (int)'A' + 1);
            char random = (char)((int)'A' + randomInt);
//            char random = (char)((int)'A' + Math.random() * ((int)'Z' - (int)'A' + 1));
            sb.append(random);
        }
        return sb.toString();
    }

    /**
     *
     * @param v
     */
    private void setLogin(View v) {
        Button loginButton = v.findViewById(R.id.login_button);
        loginButton.setOnClickListener((View view) -> {
//            login();
            loginDemo();
        });
    }

    private void loginDemo() {
        String loginURL = getActivity().getString(R.string.csso_url) + "login";
        String loginParams = "SYS_ID=teamwalk" + "&" +
                "appl_id=" + pid + "&" +
                "appl_pwd=" + passwordEditText.getText().toString() + "&" +
                "service=teamwalk" + getActivity().getString(R.string.env) + "://loginsuccess";


        Intent signInIntent = new Intent();
        signInIntent.putExtra("pid", pid);
        signInIntent.putExtra("url", loginURL);
        signInIntent.putExtra("params", loginParams);

        activity.setResult(Activity.RESULT_OK, signInIntent);
        activity.finish();
    }

    private void login() {
        if (TextUtils.isEmpty(pidEditText.getText().toString()) || pidEditText.getText().toString().length() != 10) {
            pidLayout.setBackgroundColor(requireActivity().getColor(R.color.colorError));
            pidExistWarning.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(passwordEditText.getText().toString())) {
            passwordLayout.setBackgroundColor(requireActivity().getColor(R.color.colorError));
            passwordExistWarning.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(captchaEditText.getText().toString()) || !genText.equals(captchaEditText.getText().toString().toUpperCase(Locale.ENGLISH))) {
            captchaLayout.setBackgroundColor(requireActivity().getColor(R.color.colorError));
            captchaExistWarning.setVisibility(View.VISIBLE);

            genText = genRandomNumbers();
            captchaButton.setText(genText);
            return;
        }
        if (pidExistWarning.getVisibility() == View.VISIBLE) {
            return;
        }

        String loginURL = getActivity().getString(R.string.csso_url) + "login";
        String loginParams = "SYS_ID=teamwalk" + "&" +
                "appl_id=" + pid + "&" +
                "appl_pwd=" + passwordEditText.getText().toString() + "&" +
                "service=teamwalk" + getActivity().getString(R.string.env) + "://loginsuccess";

        Intent signInIntent = new Intent();
        signInIntent.putExtra("pid", pid);
        signInIntent.putExtra("url", loginURL);
        signInIntent.putExtra("params", loginParams);

        queryUser(pid, signInIntent);
    }
}