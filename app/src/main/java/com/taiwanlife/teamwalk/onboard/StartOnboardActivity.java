package com.taiwanlife.teamwalk.onboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.taiwanlife.teamwalk.util.CelebrusCSAUtil;
import com.taiwanlife.teamwalk.util.DeviceUtil;

/**
 * Author : Ryans
 * Date : 2021/11/29
 * Introduction :
 */
public class StartOnboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceUtil.setFlagSecure(this);
        Intent intent = new Intent(this, AvatarActivity.class);
        intent.setData(getIntent().getData());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        CelebrusCSAUtil.start(this);
    }
}
