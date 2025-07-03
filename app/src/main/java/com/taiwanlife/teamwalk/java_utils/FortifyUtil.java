package com.taiwanlife.teamwalk.java_utils;


import android.content.Context;
import android.text.TextUtils;

import com.taiwanlife.teamwalk.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FortifyUtil {

    public static String filterCookiesForFortify(String cookies) {
        String newCookies = "";
        try{
            if (!TextUtils.isEmpty(cookies)) {
                String[] cookieStringArray = cookies.split(";");
                for (String cookie : cookieStringArray ){
                    String[] cookieArray = cookie.split("=");
                    String cookieKey = "";
                    String cookieValue = "";
                    if(cookieArray.length>=2){
                        cookieKey = cookieArray[0];
                        cookieValue = cookieArray[1];
                    }else if(cookieArray.length==1){
                        cookieKey = cookieArray[0];
                        cookieValue = "";
                    }else{
                        cookieKey = "";
                        cookieValue = "";
                        continue;
                    }
                    String newCookieValue="";
                    try {
                        newCookieValue = new String(cookieValue.getBytes("UTF-8"), "ISO-8859-1");
    //                    String regex = "[`~!@#$%^&*()\\+\\=\\{}|:\"?><【】\\/r\\/n]";
                        String regex = "[;+={}><【】\r\n]";
                        Pattern pa = Pattern.compile(regex);
                        Matcher ma = pa.matcher(cookieValue);
                        if(ma.find()){
                            newCookieValue = ma.replaceAll("");
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                    if(newCookieValue!=null && newCookieValue.length()>0){
                        if(newCookies.length()>0)
                            newCookies = newCookies+";"+cookieKey+"="+newCookieValue;
                        else
                            newCookies = cookieKey+"="+newCookieValue;
                    }
                }
            }
        }catch (Exception e){}
        return newCookies;
    }


    public static String[] getBuildInfo(Context context){
        String buildType = BuildConfig.BUILD_TYPE;

        final List<Map> baseInfos = new ArrayList<Map>() {{
            add(new HashMap<String, String>() {{
                put("type", "release");
                put("csso_url", "https://csso.taiwanlife.com/csso/");
                put("web_url", "https://teamwalk.taiwanlife.com/frontend/");
            }});
            add(new HashMap<String, String>() {{
                put("type", "debug");
                put("csso_url", "https://cssouat.taiwanlife.com/csso/");
                put("web_url", "http://teamwork-frontend.bestamina.net:8080/frontend/");
            }});
            add(new HashMap<String, String>() {{
                put("type", "sit");
                put("csso_url", "https://cssouat.taiwanlife.com/csso/");
                put("web_url", "http://10.1.242.55:9080/frontend/");
            }});
            add(new HashMap<String, String>() {{
                put("type", "uat");
                put("csso_url", "https://cssouat.taiwanlife.com/csso/");
                put("web_url", "https://teamwalkuat.taiwanlife.com/frontend/");
            }});
        }};

//        Patterns.WEB_URL.matcher(context.getString(R.string.csso_url)).matches();

        String csso_url = "";
        String web_url = "";
        for (Map<String, String> info: baseInfos){
            csso_url = "";
            web_url = "";
            try {
                String type = info.get("type");
                if(type.compareToIgnoreCase(buildType)==0){
                    csso_url = info.get("csso_url");
                    web_url = info.get("web_url");
                    break;
                }

            }catch (Exception e){
                csso_url = "";
                web_url = "";
            }
        }
        return new String[]{csso_url,web_url};
    }

    public static boolean checkFileName (String fileName){
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        // 判断文件名中是否包含/ \ .
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains(".")) {
            return false;
        }

        return true;
    }

    private final static String DOT_STR = ".";
    public static boolean checkFileName (String fileName, String... fileTypes){
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        if (null == fileTypes || fileTypes.length == 0) {
            checkFileName(fileName);
        }

        boolean flag = false;
        int fileEnd = 0;
        for (String fileType : fileTypes) {
            if (!fileType.startsWith(DOT_STR)) {
                fileType = DOT_STR + fileType;
            }

            if (fileName.endsWith(fileType)) {
                flag = true;
                fileEnd = fileType.length();
                break;
            }
        }

        // 文件类型不匹配
        if (!flag) {
            return false;
        }

        // 判断文件名中是否包含/ \
        // 以及在去除文件后缀 + . 的情况下是否包含 .
        if (fileName.contains("/") || fileName.contains("\\")
                || fileName.substring(0, fileName.length() - fileEnd).contains(".")) {
            return false;
        }

        return true;
    }
}
