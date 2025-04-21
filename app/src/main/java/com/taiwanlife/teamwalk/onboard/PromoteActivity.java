/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.onboard;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.util.AbstractTextValidator;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public class PromoteActivity extends OnboardActivity {

    private static final String TAG = "PromoteActivity";

    private EditText promoteCodeEdit;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote);

        setSkip();
        setDialog();
        setPromoteCodeEdit();
        setNext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUser();
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

    private void setDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.fragment_dialog, null);

        TextView infoText = dialogView.findViewById(R.id.onboarding_dialog_textContainer);
        infoText.setText(Html.fromHtml(getResources().getString(R.string.onboard_promote_info), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));

        Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        dialog.setContentView(dialogView);

        ImageButton infoButton = findViewById(R.id.onboarding_imageView_info);
        infoButton.setOnClickListener((View v) -> {
            dialog.show();
        });

        Button closeButton = dialogView.findViewById(R.id.onboarding_dialog_close);
        closeButton.setOnClickListener((View v) -> {
            dialog.dismiss();
        });
    }

    private void setPromoteCodeEdit() {
        promoteCodeEdit = findViewById(R.id.onboarding_editText_promote_code);
        promoteCodeEdit.addTextChangedListener(new AbstractTextValidator(promoteCodeEdit) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(PromoteActivity.this, getString(R.string.onboard_promote_editText) + " " + getString(R.string.empty), Toast.LENGTH_LONG).show();
                } else {
                    if (getMyUser() != null) {
                        getMyUser().setReferrerCode(text);
                    }
                }
            }
        });

//        if (user.getReferrerCode().length() != 0) {
//            promoteCodeEdit.setText(user.getReferrerCode());
//            promoteCodeEdit.setEnabled(false);
//        }
    }

    private void setNext() {
        Button nextButton = findViewById(R.id.onboarding_next_button);
        nextButton.setOnClickListener((View view) -> {
            Editable text = promoteCodeEdit.getText();
            String promote = text.toString();
            if (promote.length() > 0 && promote.length() < 8) {
                Toast.makeText(PromoteActivity.this, getString(R.string.onboard_promote_msg), Toast.LENGTH_SHORT).show();
                return;
            }
            if (getMyUser() != null) {
                getMyUser().setCompleteOnboarding(true);
                updateUser(getMyUser(), true);
            }

            Intent intent = new Intent(PromoteActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(MainActivity.ON_BOARD_FINISH);
            startActivity(intent);
        });
    }
}