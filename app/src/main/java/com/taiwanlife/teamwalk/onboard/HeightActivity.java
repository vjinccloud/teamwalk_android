/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.onboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kevalpatel2106.rulerpicker.RulerValuePicker;
import com.kevalpatel2106.rulerpicker.RulerValuePickerListener;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.util.AbstractTextValidator;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public class HeightActivity extends OnboardActivity {

    private static final String TAG = "HeightActivity";

    private int DEFAUL_HEIGHT = 170;

    private EditText heightEdit;
    private  RulerValuePicker rulerValuePicker;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);

//        setSkip();
        setHeightEdit();
        setHeightRuler();
        setNext();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    private void setSkip() {
//        Button skipButton = findViewById(R.id.onboarding_skip_button);
//        skipButton.setOnClickListener((View v) -> {
//            if (user != null) {
//                user.setCompleteOnboarding(true);
//                updateUser(user, true);
//            }
//
//            Intent backToMainIntent = new Intent(this, MainActivity.class);
//            backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(backToMainIntent);
//            finish();
//        });
//    }

    private void setHeightEdit() {
        heightEdit = findViewById(R.id.onboarding_editTextNumberDecimal_height);
        heightEdit.addTextChangedListener(new AbstractTextValidator(heightEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(HeightActivity.this, getString(R.string.onboard_height) + " " + getString(R.string.empty), Toast.LENGTH_LONG).show();
                } else {
                    float height = Float.parseFloat(text);

                    if (getMyUser() != null) {
                        getMyUser().setHeight(height);
                    }
                }
            }
        });
    }

    private void setHeightRuler() {
        rulerValuePicker = findViewById(R.id.onboarding_ruler_picker);
        rulerValuePicker.selectValue(DEFAUL_HEIGHT);

        rulerValuePicker.setValuePickerListener(new RulerValuePickerListener() {
            @Override
            public void onValueChange(int selectedValue) {
                heightEdit.setText(String.valueOf((float) selectedValue));
            }

            @Override
            public void onIntermediateValueChange(int selectedValue) {
                heightEdit.setText(String.valueOf((float) selectedValue));
            }
        });
    }

    private void setNext() {
        Button nextButton = findViewById(R.id.onboarding_next_button);
        nextButton.setOnClickListener((View view) -> {
            String text = heightEdit.getText().toString();
            float height = Float.parseFloat(text);
            String[] split = text.split("\\.");
            if (height < 100 || height > 250 ) {
                Toast.makeText(HeightActivity.this, getString(R.string.onboarding_height_msg), Toast.LENGTH_SHORT).show();
            }
            if (split.length > 1 && (split[1] != null && split[1].length() > 1)) {
                Toast.makeText(HeightActivity.this, getString(R.string.onboarding_height_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            if (getMyUser() != null) {
                getMyUser().setHeight(Float.parseFloat(heightEdit.getText().toString()));
                updateUser(getMyUser(), true);
            }

            Intent intent = new Intent(this, WeightActivity.class);
            startActivity(intent);
        });
    }
}