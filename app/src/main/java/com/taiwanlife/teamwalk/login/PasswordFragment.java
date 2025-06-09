/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import static com.taiwanlife.teamwalk.login.CSSOWebViewActivity.CSSO_SIGN_UP;

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

import java.security.SecureRandom;
import java.util.Locale;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PasswordFragment extends Fragment implements LoginMethod {


    private Login loginContext;
    private SecuredPreferenceStore sharedPref;
    private boolean isRememberMe;
    private String pid;
    private EditText pidEditText;
    private EditText passwordEditText;
    private EditText captchaEditText;
    private String genText;
    private Button captchaButton;
    private Activity activity;

    View pidLayout;
    TextView pidExistWarning;
    View passwordLayout;
    TextView passwordExistWarning;

    View captchaLayout;
    TextView captchaExistWarning;

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

    @Override
    public void setOnFragmentAttachedListener(Login loginContext) {
        this.loginContext = loginContext;
    }

    @Override
    public void queryUser(String pid, Intent intent) {
        SecuredPreferenceStore.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("csso", CSSO_SIGN_UP);
        prefEditor.apply();

        if (intent != null) {
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
    }

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

    private void setPID(View v) {
        pidLayout = v.findViewById(R.id.login_layout_password_pid);
        pidExistWarning = v.findViewById(R.id.login_textView_warning_pid);
        pidEditText = v.findViewById(R.id.login_editText_password_pid);
        pidEditText.addTextChangedListener(new AbstractTextValidator(pidEditText) {
            @Override
            public void validate(TextView textView, String text) {
                if (pidExistWarning.getVisibility() == View.VISIBLE) {
                    pidLayout.setBackground(null);
                    pidExistWarning.setVisibility(View.GONE);
                }

                if (TextUtils.isEmpty(text) || text.length() != 10) return;

                if (text.contains("*****")) {
                    text = pid;
                }

                pid = text.toUpperCase();

                if (isRememberMe) {
                    SecuredPreferenceStore.Editor editor = sharedPref.edit();
                    editor.putString(getString((R.string.pref_login_pid)), pid);
                    editor.apply();
                }
            }
        });
    }

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

    private void setRandomNumbersGen(View v) {
        captchaButton = v.findViewById(R.id.login_button_captcha);
        captchaButton.setOnClickListener((View view) -> {
            genText = genRandomNumbers();
            captchaButton.setText(genText);
        });

        genText = genRandomNumbers();
        captchaButton.setText(genText);
    }

    private String genRandomNumbers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            SecureRandom secureRandom = new SecureRandom();
            int randomInt = secureRandom.nextInt((int) 'Z' - (int) 'A' + 1);
            char random = (char) ((int) 'A' + randomInt);
            sb.append(random);
        }
        return sb.toString();
    }

    private void setLogin(View v) {
        Button loginButton = v.findViewById(R.id.login_button);
        loginButton.setOnClickListener((View view) -> {
            login();
        });
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

        String loginURL = getActivity().getString(R.string.csso_url) + "mock/csso";
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