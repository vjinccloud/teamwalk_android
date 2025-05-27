/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.onboard;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.model.UserAvatar;
import com.taiwanlife.teamwalk.util.AbstractTextValidator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import recycler.coverflow.CoverFlowLayoutManger;
import recycler.coverflow.RecyclerCoverFlow;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class AvatarActivity extends OnboardActivity {

    private static final String TAG = "AvatarActivity";

    private RecyclerCoverFlow recyclerCoverFlow;
    private AvaterAdapter avatarAdapter;

    private String avatarColor = "red";

    private EditText editText;

    private UserAvatar userAvatar;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);

        setSkip();
        setAvatars();
        setColorPicker();
        setNickname();
        setNext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();
        if (uri != null) {

            String scheme_action = uri.getHost();

            if (TextUtils.equals(scheme_action, "onboarding")) {

                String username = uri.getQueryParameter("username");
                String ticket = uri.getQueryParameter("ticket");
                String token = uri.getQueryParameter("token");
                String refreshToken =  uri.getQueryParameter("refreshToken");
                Long exp = Long.valueOf(uri.getQueryParameter("exp"));

                storeUser(username, ticket, token, refreshToken, exp);
            }
        }

        getUser();
    }

    @Override
    public void onBackPressed() {
    }

    private void setSkip() {
        Button skipButton = findViewById(R.id.onboarding_skip_button);
        skipButton.setOnClickListener((View v) -> {
            Log.e("GGG", "LoginDemo 頭像頁面返回");
            Intent backToMainIntent = new Intent(this, MainActivity.class);
            startActivity(backToMainIntent);
            finish();
//            if (user != null) {
//                user.setCompleteOnboarding(true);
//                updateUser(user, true);
//            }
//
//            Intent backToMainIntent = new Intent(this, MainActivity.class);
//            backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(backToMainIntent);
//            finish();
        });
    }

    private void setAvatars() {
        recyclerCoverFlow = findViewById(R.id.onboarding_recyclerCoverFlow_avatar);

        userAvatar = new UserAvatar("2.png", "", "0.png");
        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_red_options));
        recyclerCoverFlow.setAdapter(avatarAdapter);
        recyclerCoverFlow.setIntervalRatio((float) 0.66);
        recyclerCoverFlow.setAlphaItem(true);
        recyclerCoverFlow.setOnItemSelectedListener(new CoverFlowLayoutManger.OnSelected() {
            @Override
            public void onItemSelected(int position) {
                int avatar = position + 1;
                int filter = 0;
                switch (avatarColor) {
                    case "red":
                        avatar += 0;
//                        filter = 1;
                        break;
                    case "orange":
                        avatar += 6;
//                        filter = 2;
                        break;
                    case "yellow":
                        avatar += 12;
//                        filter = 3;
                        break;
                    case "blue":
                        avatar += 18;
//                        filter = 4;
                        break;
                    case "purple":
                        avatar += 24;
//                        filter = 5;
                        break;
                    case "royalpurple":
                        avatar += 30;
//                        filter = 6;
                        break;
                    default:
                        avatar += 0;
                        break;

                }

                userAvatar = new UserAvatar(String.valueOf(avatar) + ".png", "", String.valueOf(filter) + ".png");
            }
        });
        CoverFlowLayoutManger coverFlowLayoutManger = recyclerCoverFlow.getCoverFlowLayout();
        coverFlowLayoutManger.scrollToPosition(1);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.onboarding_image_avatar);
        }
    }

    private class AvaterAdapter extends RecyclerView.Adapter {

        private TypedArray drawableList;

        public AvaterAdapter(TypedArray drawableList) {
            this.drawableList = drawableList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder) holder).iv.setImageResource(drawableList.getResourceId(position, 0));
        }

        @Override
        public int getItemCount() {
            return drawableList.length();
        }
    }

    private void setColorPicker() {
        List<Integer> pickerIds = Arrays.asList(R.id.onboarding_imageButton_red, R.id.onboarding_imageButton_orange,
                R.id.onboarding_imageButton_yellow, R.id.onboarding_imageButton_blue,
                R.id.onboarding_imageButton_purple, R.id.onboarding_imageButton_royalpurple);

        List<ImageButton> pickers = pickerIds.stream().map(rid -> (ImageButton) findViewById(rid)).collect(Collectors.toList());
        pickers.forEach(picker -> {
            picker.setOnClickListener((View view) -> {
                String resoucreName = getResources().getResourceName(picker.getId());
                String pickerType = resoucreName.substring(resoucreName.lastIndexOf("_") + 1);

                switch (pickerType) {
                    case "red":
                        avatarColor = "red";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_red_options));
                        break;
                    case "orange":
                        avatarColor = "orange";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_orange_options));
                        break;
                    case "yellow":
                        avatarColor = "yellow";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_yellow_options));
                        break;
                    case "blue":
                        avatarColor = "blue";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_blue_options));
                        break;
                    case "purple":
                        avatarColor = "purple";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_purple_options));
                        break;
                    case "royalpurple":
                        avatarColor = "royalpurple";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_royalpurple_options));
                        break;
                    default:
                        avatarColor = "red";
                        avatarAdapter = new AvaterAdapter(getResources().obtainTypedArray(R.array.avatar_red_options));
                        break;
                }

                recyclerCoverFlow.setAdapter(avatarAdapter);
                CoverFlowLayoutManger coverFlowLayoutManger = recyclerCoverFlow.getCoverFlowLayout();
                coverFlowLayoutManger.scrollToPosition(1);
            });
        });
    }

    private void setNickname() {
        editText  = findViewById(R.id.onboarding_editText_nickname);
        editText.addTextChangedListener(new AbstractTextValidator(editText) {
            private int maxLength = 8;
            private int currentEnd = 0;

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                currentEnd = start + count;
                String text = textView.getText().toString().trim();
                validate(textView, text);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                while (calculateLength(s) > maxLength) {
                    currentEnd--;
                    s.delete(currentEnd, currentEnd + 1);
                    String text = s.toString().trim();
                    validate(textView, text);
                }
            }

            protected int calculateLength(final CharSequence c) {
                int len = 0;
                final int l = c.length();
                for (int i = 0; i < l; i++) {
                    final char tmp = c.charAt(i);
                    if (tmp >= 0x20 && tmp <= 0x7E) {
                        len++;
                    } else {
                        len += 2;
                    }
                }
                return len;
            }

            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(AvatarActivity.this, getString(R.string.onboard_edit_nickname) + " " + getString(R.string.empty), Toast.LENGTH_LONG).show();
                } else {
                    if (getMyUser() != null) {
                        getMyUser().setNickName(text);
                    }
                }
            }
        });
    }

    private void setNext() {
        Button nextButton = findViewById(R.id.onboarding_next_button);
        nextButton.setOnClickListener((View view) -> {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
            Log.e("GGG", "LoginDemo 頭像頁面");
//            String  text = editText.getText().toString().trim();
//            if (!TextUtils.isEmpty(text)) {
//                char[] chars = text.toCharArray();
//                for (char aChar : chars) {
//                    int type = Character.getType(aChar);
//                    if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
//                        Toast.makeText(AvatarActivity.this, getString(R.string.onboard_edit_nickname_emoji), Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//            }
//            if (getMyUser() != null) {
//                getMyUser().setAvatar(userAvatar);
//                updateUser(getMyUser(), false);
//            }
//
//            Intent intent = new Intent(this, HeightActivity.class);
//            startActivity(intent);
        });
    }
}