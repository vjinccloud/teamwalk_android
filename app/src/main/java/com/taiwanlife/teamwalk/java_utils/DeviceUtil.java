package com.taiwanlife.teamwalk.java_utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.taiwanlife.teamwalk.R;

import java.io.File;
import java.util.List;

/**
 * Author : Ryans
 * Date : 2023/5/22
 * Introduction :
 */
public class DeviceUtil {

    public static boolean isDeviceRooted() {
        return checkBuildTags() || checkSuperUserApk() || checkFilePath();
    }
    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkSuperUserApk() {
        return new File("/system/app/Superuser.apk").exists();
    }

    private static boolean checkFilePath() {
        String[] paths = { "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su" };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }


    //檢查設備是否設置了鎖屏密碼、PIN 或圖案
    public static boolean isDeviceSecure(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            // 檢查設備是否設置了鎖屏密碼、PIN 或圖案
            return keyguardManager.isDeviceSecure();
        }
        return false; // 如果無法獲取 KeyguardManager，則返回不安全
    }
    //檢查是否啟用了鎖屏（更寬鬆的判斷）
    public boolean isKeyguardEnabled(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            return keyguardManager.isKeyguardLocked(); // 是否啟用鎖屏
        }
        return false;
    }

    public static void setFlagSecure(Activity activity) {
        activity.runOnUiThread(() -> {
            //窗口设置 FLAG_SECURE，以防止截图、录屏或其他应用程序覆盖窗口
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

            //窗口的优先级提升，以防止其不会被其他窗口覆盖
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            activity.getWindow().setAttributes(params);

            try {
                //防止应用在被其他窗口（例如悬浮窗或遮罩）覆盖时响应触摸事件
                View rootview = activity.findViewById(android.R.id.content);
                if (rootview != null) {
                    rootview.setFilterTouchesWhenObscured(true);
                }

                //隐藏其他应用的悬浮窗（Overlay Windows），从而防止悬浮窗干扰当前应用的用户交互。
                Window window = activity.getWindow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.setHideOverlayWindows(true);
                }
            } catch (Exception e) {
                Log.d("setFlagSecure", "setFilterTouchesWhenObscured & setHideOverlayWindows error");
            }

        });
    }
    public static void clearFlagSecure(Activity activity) {
        activity.runOnUiThread(() -> {
            // 移除 FLAG_SECURE，允许截图、录屏等操作
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    //        // 恢复窗口的默认类型
    //        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
    //        params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
    //        activity.getWindow().setAttributes(params);
        });
    }
    //检测是否存在其他窗口覆盖
    public static boolean isOverlayPresent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return false;
    }

    //檢測frida-server、xposed、ida 等工具在運行
    public static boolean isReverseToolRunning(Context context) {
	    String[] blacklistedProcesses = {"frida-server", "xposed", "ida"};
	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
	        for (String blacklisted : blacklistedProcesses) {
	            if (processInfo.processName.contains(blacklisted)) {
	                return true; // 檢測到逆向工具
	            }
	        }
	    }
	    return false; // 未檢測到
	}

    private static boolean isDialogShown = false; // 避免重复弹出对话框

    // 检测应用是否在前台运行
    private static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && processInfo.processName.equals(context.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // 判断是否被覆盖
    public static boolean isCovered(Context context) {
        if (Settings.canDrawOverlays(context)) {
            // 如果有悬浮窗权限，无法进一步判断覆盖，只能假定覆盖存在
            return true;
        } else {
            // 如果没有悬浮窗权限，仅检测是否在前台
            return !isAppInForeground(context);
        }
    }

    // 检测是否被覆盖并提示
    public static boolean checkCovered(Context context) {
        boolean isCovered = isCovered(context);
        if (isCovered && !isDialogShown) { // 避免重复弹出
            isDialogShown = true;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("提醒您，屏幕被覆盖，APP可能会有信息外流的风险。")
                    .setIcon(R.mipmap.ic_launcher)
                    .setCancelable(false)
                    .setPositiveButton("我知道了并继续使用", (dialog, which) -> {
                        dialog.dismiss();
                        isDialogShown = false; // 允许再次弹出
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return isCovered;
    }
}
