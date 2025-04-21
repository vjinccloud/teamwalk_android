package com.taiwanlife.teamwalk.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.taiwanlife.teamwalk.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    private final static String TAG = "PermissionUtil";

    public static final int permissionRequestCode = 1011;//权限请求码

    private static List<String> getPermissionList(){
        List<String> permissionList = new ArrayList<>();
        permissionList.clear();
        permissionList.add(Manifest.permission.INTERNET);
        permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissionList.add(Manifest.permission.CAMERA);
//        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            permissionList.add(Manifest.permission.READ_MEDIA_AUDIO);
//            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
//            permissionList.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return permissionList;
    }

    public static boolean checkPermission(Activity activity) {
        boolean hasPermissionDismiss = false;
        List<String> permissionList = getPermissionList();

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissionList.size(); i++) {
            if (ContextCompat.checkSelfPermission(activity, permissionList.get(i)) != PackageManager.PERMISSION_GRANTED) {
                hasPermissionDismiss = true;
            }
        }
        //申请权限
        if (hasPermissionDismiss) {//有权限没有通过，需要申请
            Log.i(TAG, "hasPermission: requestPermissions");
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), permissionRequestCode);
        }

        return !hasPermissionDismiss;
    }
    public static void onCheckPermission(String[] permissions, int[] grantResults, Activity activity){
        List<String> permissionList = getPermissionList();
        List<String> errorpermission = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == -1) {
                errorpermission.add(permissionList.get(i));
            }
        }

        if(errorpermission.size()>0){    //如果有权限没有被允许
            for (String onePermissiion: permissions) {
                switch (onePermissiion){
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
//                    case Manifest.permission.READ_MEDIA_AUDIO:
//                    case Manifest.permission.READ_MEDIA_IMAGES:
//                    case Manifest.permission.READ_MEDIA_VIDEO:
                        Toast.makeText(activity, "讀取權限申請失敗!", Toast.LENGTH_LONG).show();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        Toast.makeText(activity, "儲存權限申請失敗!", Toast.LENGTH_LONG).show();
                        break;
                    case Manifest.permission.INTERNET:
                    case Manifest.permission.ACCESS_NETWORK_STATE:
                        Toast.makeText(activity, "網絡權限申請失敗!", Toast.LENGTH_LONG).show();
                        break;
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        Toast.makeText(activity, "位置權限申請失敗!", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }else{
//            Toast.makeText(activity, "權限申請成功!", Toast.LENGTH_LONG).show();
        }
    }
}
