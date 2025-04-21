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
public class WeightActivity extends OnboardActivity {

    private static final String TAG = "WeightActivity";

    private int DEFAUL_WEIGHT = 60;

    private EditText weightEdit;
    private RulerValuePicker rulerValuePicker;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

//        setSkip();
        setWeightEdit();
        setWeightRuler();
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

    private void setWeightEdit() {
        weightEdit = findViewById(R.id.onboarding_editTextNumberDecimal_weight);
        weightEdit.addTextChangedListener(new AbstractTextValidator(weightEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(WeightActivity.this, getString(R.string.onboard_weight) + " " + getString(R.string.empty), Toast.LENGTH_LONG).show();
                } else {
                    float weight = Float.parseFloat(text);

                    if (getMyUser() != null) {
                        getMyUser().setWeight(weight);
                    }
                }
            }
        });
    }

    private void setWeightRuler() {
        rulerValuePicker = findViewById(R.id.onboarding_ruler_picker);
        rulerValuePicker.selectValue(DEFAUL_WEIGHT);

        rulerValuePicker.setValuePickerListener(new RulerValuePickerListener() {
            @Override
            public void onValueChange(int selectedValue) {
                weightEdit.setText(String.valueOf((float) selectedValue));
            }

            @Override
            public void onIntermediateValueChange(int selectedValue) {
                weightEdit.setText(String.valueOf((float) selectedValue));
            }
        });
    }

    private void setNext() {
        Button nextButton = findViewById(R.id.onboarding_next_button);
        nextButton.setOnClickListener((View view) -> {
            String text = weightEdit.getText().toString();
            float height = Float.parseFloat(text);
            String[] split = text.split("\\.");
            if (height < 30 || height > 250 ) {
                Toast.makeText(WeightActivity.this, getString(R.string.onboarding_weight_msg), Toast.LENGTH_SHORT).show();
            }
            if (split.length > 1 && (split[1] != null && split[1].length() > 1)) {
                Toast.makeText(WeightActivity.this, getString(R.string.onboarding_weight_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            if (getMyUser() != null) {
                getMyUser().setWeight(Float.parseFloat(weightEdit.getText().toString()));
                updateUser(getMyUser(), true);
            }

            Intent intent = new Intent(this, IntervalActivity.class);
            startActivity(intent);
        });
    }
}