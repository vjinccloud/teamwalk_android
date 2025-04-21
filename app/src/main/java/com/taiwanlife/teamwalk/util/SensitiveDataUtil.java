package com.taiwanlife.teamwalk.util;


/* Author:  MoonZhao
 * Time:    2024/12/12 10:19
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.taiwanlife.teamwalk.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SensitiveDataUtil {
    private final static String TAG = SensitiveDataUtil.class.getSimpleName();

    public static void clearWebViewSensitiveData(Context context, WebView webView, boolean isAll) {
        try{
            // 清除 Cookies
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(null);
            cookieManager.flush();

            // 清除 WebView 缓存
            webView.clearCache(true);
            // 清除 WebView 表单数据
            webView.clearFormData();
            // 清除 WebView 历史记录
            if(isAll)
                webView.clearHistory();
        }catch (Exception ignored){}
    }
    public static void backupWebViewSensitiveData(Context context, WebView webView) {
        try{
            backupCookies(context);
        }catch (Exception ignored){}
    }
    public static void restoreWebViewSensitiveData(Context context, WebView webView) {
        try{
            restoreCookies(context);
        }catch (Exception ignored){}
    }

    private final static String cookieBackupPrefsFileName = "webview_cookie_backup_prefs";
    private static List<String> getWebViewCookiesUrls(Context context){
        String csso_url = context.getString(R.string.csso_url);
        String web_origin_url = context.getString(R.string.origin);
        String tcav_url = context.getString(R.string.tcav_url);
        List<String>retVal = new ArrayList<>();
        retVal.add(csso_url);
        retVal.add(web_origin_url);
        retVal.add(tcav_url);
        return retVal;
    }
    private static void backupCookies(Context context) {
        List<String> urls = getWebViewCookiesUrls(context);
        CookieManager cookieManager = CookieManager.getInstance();
        try {
            String namePrefs = cookieBackupPrefsFileName+context.toString();
            SharedPreferences sharedPreferences = context.getSharedPreferences(namePrefs, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (String url : urls) {
                String cookie = cookieManager.getCookie(url);
                if (cookie != null) {
                    editor.putString(url, cookie); // 加密存储由库自动处理
                }
            }
            editor.apply();
        } catch (Exception e) {
            Log.d("WebView Cookies", "backupCookies: "+e.toString());
//            e.printStackTrace();
        }
    }
    private static void restoreCookies(Context context) {
        try {
            String namePrefs = cookieBackupPrefsFileName+context.toString();
            SharedPreferences sharedPreferences = context.getSharedPreferences(namePrefs, Context.MODE_PRIVATE);
            Map<String, ?> allCookies = sharedPreferences.getAll();

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            for (Map.Entry<String, ?> entry : allCookies.entrySet()) {
                String url = entry.getKey();
                String cookies = (String) entry.getValue();
                if (cookies != null) {
                    cookieManager.setCookie(url, cookies);
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.createInstance(context);
                CookieSyncManager.getInstance().sync();
            } else {
                cookieManager.flush();
            }
        } catch (Exception e) {
            Log.d("WebView Cookies", "restoreCookies: error");
        }
    }
}
