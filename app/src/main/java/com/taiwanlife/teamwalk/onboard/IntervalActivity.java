/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.onboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public class IntervalActivity extends OnboardActivity {

    private static final String TAG = "IntervalActivity";

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval);

        setSkip();
        setSeekBar();
        setNext();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setSkip() {
        Button skipButton = findViewById(R.id.onboarding_skip_button);
        skipButton.setOnClickListener((View v) -> {
            if (getMyUser() != null) {
                getMyUser().setCompleteOnboarding(true);
                updateUser(getMyUser(), true);
            }

            Intent backToMainIntent = new Intent(this, MainActivity.class);
            backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            backToMainIntent.setAction(MainActivity.ON_BOARD_FINISH);
            startActivity(backToMainIntent);
            finish();
        });
    }

    private void setSeekBar() {
        IndicatorSeekBar indicatorSeekBar = findViewById(R.id.onboarding_indicatorSeekBar);
        indicatorSeekBar.setIndicatorTextFormat("${TICK_TEXT}");

        ImageView statusImage = findViewById(R.id.onboarding_imageView_status);
        ImageView stateImage1 = findViewById(R.id.onboarding_imageView_state_0);
        ImageView stateImage2 = findViewById(R.id.onboarding_imageView_state_1);
        ImageView stateImage3 = findViewById(R.id.onboarding_imageView_state_2);
        ImageView stateImage4 = findViewById(R.id.onboarding_imageView_state_3);

        indicatorSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {

            @Override
            public void onSeeking(SeekParams seekParams) {

                switch (seekParams.progress) {
                    case 1:
                        statusImage.setImageDrawable(ContextCompat.getDrawable(IntervalActivity.this, R.drawable.onboarding_health_level_1));
                        stateImage1.setVisibility(View.VISIBLE);
                        stateImage2.setVisibility(View.INVISIBLE);
                        stateImage3.setVisibility(View.INVISIBLE);
                        stateImage4.setVisibility(View.INVISIBLE);

                        if (getMyUser() != null) {
                            getMyUser().setWorkoutInterval("A");
                        }
                        break;
                    case 2:
                        statusImage.setImageDrawable(ContextCompat.getDrawable(IntervalActivity.this, R.drawable.onboarding_health_level_2));
                        stateImage1.setVisibility(View.INVISIBLE);
                        stateImage2.setVisibility(View.VISIBLE);
                        stateImage3.setVisibility(View.INVISIBLE);
                        stateImage4.setVisibility(View.INVISIBLE);

                        if (getMyUser() != null) {
                            getMyUser().setWorkoutInterval("B");
                        }
                        break;
                    case 3:
                        statusImage.setImageDrawable(ContextCompat.getDrawable(IntervalActivity.this, R.drawable.onboarding_health_level_3));
                        stateImage1.setVisibility(View.INVISIBLE);
                        stateImage2.setVisibility(View.INVISIBLE);
                        stateImage3.setVisibility(View.VISIBLE);
                        stateImage4.setVisibility(View.INVISIBLE);

                        if (getMyUser() != null) {
                            getMyUser().setWorkoutInterval("C");
                        }
                        break;
                    case 4:
                        statusImage.setImageDrawable(ContextCompat.getDrawable(IntervalActivity.this, R.drawable.onboarding_health_level_4));
                        stateImage1.setVisibility(View.INVISIBLE);
                        stateImage2.setVisibility(View.INVISIBLE);
                        stateImage3.setVisibility(View.INVISIBLE);
                        stateImage4.setVisibility(View.VISIBLE);

                        if (getMyUser() != null) {
                            getMyUser().setWorkoutInterval("D");
                        }
                        break;
                    default:
                        statusImage.setImageDrawable(ContextCompat.getDrawable(IntervalActivity.this, R.drawable.onboarding_health_level_2));
                        if (getMyUser() != null) {
                            getMyUser().setWorkoutInterval("B");
                        }
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });
    }

    private void setNext() {
        Button nextButton = findViewById(R.id.onboarding_next_button);
        nextButton.setOnClickListener((View view) -> {
            if (getMyUser() != null) {
                updateUser(getMyUser(), true);
            }

            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        });
    }
}