/**
 * Copyright (c) 1989-2020 INVENTEC BESTA Co., Ltd.
 */
package com.taiwanlife.teamwalk.login;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;
import com.taiwanlife.teamwalk.model.Annoucement;
import com.taiwanlife.teamwalk.service.AnnoucementService;
import com.taiwanlife.teamwalk.service.SysConfigService;
import com.taiwanlife.teamwalk.util.AlertDialogFragment;
import com.taiwanlife.teamwalk.util.CelebrusCSAUtil;
import com.taiwanlife.teamwalk.util.DeviceUtil;
import com.taiwanlife.teamwalk.util.GsonCreator;
import com.taiwanlife.teamwalk.util.PermissionUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;
import segmented_control.widget.custom.android.com.segmentedcontrol.item_row_column.SegmentViewHolder;

/**
 * @author Vincent.Chen
 * @version 1
 *
 * @date 2020/10/20
 */
public class LoginActivity extends AppCompatActivity implements Login, AlertDialogFragment.Builder {

    private static final String TAG = "LoginActivity";

    private SecuredPreferenceStore sharedPref;

    private int segmentControlPosition = 0;

    private String appV;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceUtil.setFlagSecure(this);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(this.getColor(R.color.white));

//        sharedPref = getSecuredPreferenceStore(getString(R.string.pref_login), LoginActivity.MODE_PRIVATE);
        sharedPref = SecuredPreferenceStore.getSharedInstance();

        // build version
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            appV = pInfo.versionName;

            TextView textView = findViewById(R.id.login_build_appv);
            textView.setText(appV);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Fail to get package version");
        }
        String uUid = sharedPref.getString(getString(R.string.pref_login_uuid), "");
        if (uUid == null || uUid.equals("")) {
            uUid = sharedPref.getString(getString(R.string.pref_login_fid), "");
            SecuredPreferenceStore.Editor edit = sharedPref.edit();
            edit.putString(getString(R.string.pref_login_uuid), uUid);
            edit.apply();
            AlertDialogFragment dialogFragment = createDialog("", getString(R.string.security_msg), R.drawable.alert_1, getString(R.string.ok));
            dialogFragment.show(getSupportFragmentManager(), TAG);
        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                PermissionUtil.checkPermission(LoginActivity.this);
//            }
//        },10);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtil.permissionRequestCode:
                PermissionUtil.onCheckPermission(permissions, grantResults, this);
                break;

            default:
                return;
        }
    }

    /**
     *
     * @param fragment
     */
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof PasswordFragment || fragment instanceof PatternFragment) {
            LoginMethod loginMethodFragment = (LoginMethod) fragment;
            loginMethodFragment.setOnFragmentAttachedListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setSegmentControl();

        // check app version
        checkAppVersion();

        // get annoucement
        getAnnoucement();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        CelebrusCSAUtil.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SecuredPreferenceStore.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_login_segment_control_pos), segmentControlPosition);
        editor.apply();

        finish();
    }

    private void setSegmentControl() {
        segmentControlPosition = sharedPref.getInt(getString(R.string.pref_login_segment_control_pos), 0);
        Log.d(TAG, "current segment position: " + segmentControlPosition);

        SegmentedControl segmentedControl = findViewById(R.id.login_segment_control);

        segmentedControl.addOnSegmentClickListener((SegmentViewHolder segmentViewHolder) -> {
            Log.d(TAG, String.valueOf(segmentViewHolder.getAbsolutePosition()));
            segmentControlPosition = segmentViewHolder.getAbsolutePosition();

            SecuredPreferenceStore.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.pref_login_segment_control_pos), segmentControlPosition);
            editor.apply();

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            switch (segmentControlPosition) {
                case 0:
                    fragmentTransaction.replace(R.id.login_fragment, new PasswordFragment());
                    break;
                case 1:
                    fragmentTransaction.replace(R.id.login_fragment, new PatternFragment());
                    break;
                default:
                    fragmentTransaction.replace(R.id.login_fragment, new PasswordFragment());
                    break;
            }

            fragmentTransaction.commit();
        });

        segmentedControl.setSelectedSegment(segmentControlPosition);
    }

    @Override
    public DialogFragment buildAlert(String title, String msg, int image, String buttonText, String positiveButtonText, Intent positiveIntent, boolean forResult, int resultCode) {
        if (TextUtils.isEmpty(positiveButtonText)) {
            return createDialog(title,msg, image, buttonText);
        } else {
            return createDialogWithPositionBtn(title, msg, image, buttonText, positiveButtonText, positiveIntent, forResult, resultCode);
        }
    }

    /**
     *
     * @param dialogFragment
     */
    @Override
    public void showAlert(DialogFragment dialogFragment) {
        dialogFragment.show(getSupportFragmentManager(), TAG);
    }

    /**
     *
     * @param title
     * @param msg
     * @param image
     * @param buttonText
     * @return
     */
    @Override
    public AlertDialogFragment createDialog(String title, String msg, int image, String buttonText) {
        return AlertDialogFragment.newInstance(title, msg, image, buttonText);
    }

    /**
     *
     * @param title
     * @param msg
     * @param image
     * @param buttonText
     * @param positiveButtonText
     * @param positiveIntent
     * @return
     */
    @Override
    public AlertDialogFragment createDialogWithPositionBtn(String title, String msg, int image, String buttonText, String positiveButtonText, Intent positiveIntent, boolean forResult, int resultCode) {
        AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(title, msg, image, buttonText);
        dialogFragment.setPositiveButton(positiveButtonText, positiveIntent, forResult, resultCode);
        return dialogFragment;
    }

    @Override
    public AlertDialogFragment createDialogWithNegativeButton(String title, String msg, int image, String negativeText, Intent negativeIntent, boolean forResult, int resultCode) {
        AlertDialogFragment dialogFragment = AlertDialogFragment.newInstance(title, msg, image, negativeText);
        dialogFragment.setNegativeButton(negativeIntent, forResult, resultCode);
        return dialogFragment;
    }

    private void checkAppVersion() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))))
                .build();

        SysConfigService sysConfigService = retrofit.create(SysConfigService.class);
        Call<Map> getAppVersionCall = sysConfigService.getAppVersion();

        getAppVersionCall.enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {
                Map config = (Map) response.body();
                if (config != null) {
                    String serverVersion = (String) config.get("android_force_upgrade");
                    if (compareVersions(serverVersion, appV)) {
                        String googlePlay = "com.android.vending";
                        String appPackageName = "com.taiwanlife.teamwalk";
                        Intent gotoStore = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                        gotoStore.setPackage(googlePlay);
                        gotoStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        AlertDialogFragment dialogFragment = createDialogWithNegativeButton("版本更新通知", "噢...你的Teamwalk不是最新版，動動手指趕快去更新吧！", R.drawable.alert_1, "立即更新", gotoStore, false, 0);
                        dialogFragment.show(getSupportFragmentManager(), TAG);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {
                Log.i(TAG, "Fail to get app version");
            }
        });
    }

    private void getAnnoucement() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create(GsonCreator.build(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))))
                .build();

        AnnoucementService annoucementService = retrofit.create(AnnoucementService.class);
        Call<List<Annoucement>> listAnnoucementsCall = annoucementService.listAnnoucements(AnnoucementService.ANNOUCEMENT);
        listAnnoucementsCall.enqueue(new Callback<List<Annoucement>>() {
            @Override
            public void onResponse(Call<List<Annoucement>> call, Response<List<Annoucement>> response) {
                List<Annoucement> annoucementList = response.body();

                if (annoucementList != null) {
//                    Collections.reverse(annoucementList);
                    for (Annoucement annoucement : annoucementList) {
                        if (annoucement.isLockLogin()) {
                            AlertDialogFragment dialogFragment = createDialog(annoucement.getTitle(), annoucement.getMessage(), R.drawable.alert_1, getString(R.string.ok));
                            showAlert(dialogFragment);

                            Date today = new Date();
                            if (today.after(annoucement.getLockStart()) && today.before(annoucement.getLockEnd())) {
                                dialogFragment.setCancelable(false);
                            }
                        } else {
                            showAlert(createDialog(annoucement.getTitle(), annoucement.getMessage(), R.drawable.alert_1, getString(R.string.ok)));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Annoucement>> call, Throwable t) {
                Log.i(TAG, "Fail to get annoucemnet: s");
            }
        });
    }

    /**
     *
     * @param myVersion 本地version
     * @param serVision Server Version
     * @return   myVersion < Server Version
     */

    private boolean compareVersion(String myVersion, String serVision) {
        String[] newV = myVersion.split("\\.");
        String[] oldV = serVision.split("\\.");
        if (newV.length == 3 && oldV.length == 3) {
            if (Integer.parseInt(newV[0]) < Integer.parseInt(oldV[0])) {
                return true;
            } else if (Integer.parseInt(newV[1]) < Integer.parseInt(oldV[1])) {
                return true;
            } else return Integer.parseInt(newV[2]) < Integer.parseInt(oldV[2]);
        }
        return false;
    }
    /**
     * 如果版本1 大于 版本2 返回true 否则返回fasle 支持 2.2 2.2.1 比较
     * 支持不同位数的比较  2.0.0.0.0.1  2.0 对比
     *
     * @param v1 版本服务器版本 " 1.1.2 "
     * @param v2 版本 当前版本 " 1.2.1 "
     * @return ture ：需要更新 false ： 不需要更新
     */
    public static boolean compareVersions(String v1, String v2) {
        //判断是否为空数据
        if (TextUtils.equals(v1, "") || TextUtils.equals(v2, "")) {
            return false;
        }
        String[] str1 = v1.split("\\.");
        String[] str2 = v2.split("\\.");
        if (str1.length == str2.length) {
            for (int i = 0; i < str1.length; i++) {
                if (Integer.parseInt(str1[i]) > Integer.parseInt(str2[i])) {
                    return true;
                } else if (Integer.parseInt(str1[i]) < Integer.parseInt(str2[i])) {
                    return false;
                } else if (Integer.parseInt(str1[i]) == Integer.parseInt(str2[i])) {

                }
            }
        } else {
            if (str1.length > str2.length) {
                for (int i = 0; i < str2.length; i++) {
                    if (Integer.parseInt(str1[i]) > Integer.parseInt(str2[i])) {
                        return true;
                    } else if (Integer.parseInt(str1[i]) < Integer.parseInt(str2[i])) {
                        return false;

                    } else if (Integer.parseInt(str1[i]) == Integer.parseInt(str2[i])) {
                        if (str2.length == 1) {
                            continue;
                        }
                        if (i == str2.length - 1) {

                            for (int j = i; j < str1.length; j++) {
                                if (Integer.parseInt(str1[j]) != 0) {
                                    return true;
                                }
                                if (j == str1.length - 1) {
                                    return false;
                                }

                            }
                            return true;
                        }
                    }
                }
            } else {
                for (int i = 0; i < str1.length; i++) {
                    if (Integer.parseInt(str1[i]) > Integer.parseInt(str2[i])) {
                        return true;
                    } else if (Integer.parseInt(str1[i]) < Integer.parseInt(str2[i])) {
                        return false;

                    } else if (Integer.parseInt(str1[i]) == Integer.parseInt(str2[i])) {
                        if (str1.length == 1) {
                            continue;
                        }
                        if (i == str1.length - 1) {
                            return false;

                        }
                    }

                }
            }
        }
        return false;
    }
}
