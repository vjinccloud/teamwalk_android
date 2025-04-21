/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.util.CelebrusCSAUtil;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private final static int SPLASH_DISPLAY_LENGTH = 3000;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String image = getIntent().getStringExtra("welcomeImage");
        if (!TextUtils.isEmpty(image)) {
            ImageView imageView = findViewById(R.id.splash_image);
            byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
            Glide.with(this).load(decodedBytes).fitCenter().into(imageView);
        };

        String message = getIntent().getStringExtra("welcomeMessage");
        if (!TextUtils.isEmpty(message))  {
            TextView messageView = findViewById(R.id.splash_text);
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
        }

        scheduleSplashScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        CelebrusCSAUtil.start(this);
    }

    private void scheduleSplashScreen() {
        new Handler().postDelayed(() -> {
            setResult(Activity.RESULT_OK, new Intent());
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}