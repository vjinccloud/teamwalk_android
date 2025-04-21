/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.util;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.taiwanlife.teamwalk.R;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/21
 */
public class AlertDialogFragment extends DialogFragment {

    public static final String TAG = "AlertDialogFragment";

    private static final String ARG_TITLE = "title";
    private static final String ARG_BODY = "body";
    private static final String ARG_IMAGE = "image";
    private static final String ARG_NEGATIVE_TEXT = "nText";

    private Integer bodyTextAlign;
    private String title;
    private String body;
    private int image;
    private boolean hasPositiveButton = false;
    private String negativeText;
    private String positiveText;
    private Intent positiveIntent;
    private Intent negativeIntent;
    private boolean forResult = false;
    private int resultcode = 0;
    private PrepareIntent prepareIntent;

    public AlertDialogFragment() {
        // Required empty public constructor
    }

    /**
     *
     * @param title
     * @param body
     * @param image
     * @param negativeText
     * @return
     */
    public static AlertDialogFragment newInstance(String title, String body, int image, String negativeText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_BODY, body);
        args.putInt(ARG_IMAGE, image);
        args.putString(ARG_NEGATIVE_TEXT, negativeText);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            body = getArguments().getString(ARG_BODY);
            image = getArguments().getInt(ARG_IMAGE);
            negativeText = getArguments().getString(ARG_NEGATIVE_TEXT);
        }
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.fragment_alert_dialog, null);

        ImageView imageView = layout.findViewById(R.id.alert_image);
        Drawable imageDrawable = ContextCompat.getDrawable(getActivity(), image);
        if (imageDrawable != null) {
            imageView.setImageDrawable(imageDrawable);
        }

        TextView titleView = layout.findViewById(R.id.alert_title);
        titleView.setText(title);

        TextView bodyView = layout.findViewById(R.id.alert_body);
        bodyView.setText(body);
        if (bodyTextAlign != null) {
            bodyView.setTextAlignment(bodyTextAlign);
        }

        if (hasPositiveButton) {
            Button positiveButton = layout.findViewById(R.id.alert_button_positive);
            positiveButton.setText(positiveText);
            positiveButton.setOnClickListener((View v) -> {
                if (prepareIntent != null) {
                    prepareIntent.preparePositive();
                }
                if (forResult) {
                    getActivity().startActivityForResult(positiveIntent, resultcode);
                    dismiss();
                } else {
                    getActivity().startActivity(positiveIntent);
                    dismiss();
                }
            });
            positiveButton.setVisibility(View.VISIBLE);
        }

        Button negativeButton = layout.findViewById(R.id.alert_button_negative);
        negativeButton.setText(negativeText);
        negativeButton.setOnClickListener((View v) -> {
            if (prepareIntent != null) {
                prepareIntent.prepareNegative();
            }
            if (negativeIntent != null && forResult) {
                getActivity().startActivityForResult(negativeIntent, resultcode);
            } else if (negativeIntent != null) {
                getActivity().startActivity(negativeIntent);
            }
            dismiss();
        });

        View buttonGroup = layout.findViewById(R.id.alert_button_group);
        if (!isCancelable()) {
            buttonGroup.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);

        return builder.create();
    }

    public interface Builder {
        public AlertDialogFragment createDialog(String title, String msg, int image, String buttonText);
        public AlertDialogFragment createDialogWithPositionBtn(String title, String msg, int image, String buttonText, String positiveButtonText, Intent positiveIntent, boolean forResult, int resultCode);
        public AlertDialogFragment createDialogWithNegativeButton(String title, String msg, int image,  String negativeText, Intent negativeIntent, boolean forResult, int resultCode);
    }

    public interface PrepareIntent {
        void preparePositive();
        void prepareNegative();
    }

    /**
     *
     * @param positiveText
     * @param positiveIntent
     */
    public void setPositiveButton(String positiveText, Intent positiveIntent, boolean forResult, int resultCode) {
        if (!TextUtils.isEmpty(positiveText) && positiveIntent != null) {
            this.hasPositiveButton = true;
            this.positiveText = positiveText;
            this.positiveIntent = positiveIntent;
            this.forResult = forResult;
            this.resultcode = resultCode;
        }
    }

    /**
     *
     * @param negativeIntent
     */
    public void setNegativeButton(Intent negativeIntent, boolean forResult, int resultCode) {
        if (negativeIntent != null) {
            this.negativeIntent = negativeIntent;
            this.forResult = forResult;
            this.resultcode = resultCode;
        }
    }

    public void setBodyTextAlign(int align) {
        bodyTextAlign  = align;
    }

    public void setPrepareIntent(PrepareIntent prepareIntent) {
        this.prepareIntent = prepareIntent;
    }
}