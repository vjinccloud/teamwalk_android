package com.taiwanlife.teamwalk.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.speed_trap.android.Celebrus;
import com.speed_trap.android.Communications;
import com.speed_trap.android.OperationalMode;
import com.speed_trap.android.UseCase;
import com.speed_trap.android.automatic.AutoInstrumentApiLevel14;
import com.speed_trap.android.automatic.InstrumentationOption;
import com.taiwanlife.teamwalk.BuildConfig;

public class CelebrusCSAUtil {
    public static final String CELEBRUSCSA_csaName = "twlifecsa";
    public static final String CELEBRUSCSA_collectionUrl_UAT = "https://dmpuat.taiwanlife.com";
    public static final String CELEBRUSCSA_collectionUrl_PROD = "https://dmp.taiwanlife.com";
    public static final String CELEBRUSCSA_appName = "app://com.taiwanlife.mobileapp.android.teamwalk";

    public static final String domains = ".taiwanlife.com";


    public static void start(Activity activity){
        String collectionUrl = "";
        String buildType = BuildConfig.BUILD_TYPE;
        switch (buildType){
            case "release":
                collectionUrl = CELEBRUSCSA_collectionUrl_PROD;
                break;
            case "uat":
                collectionUrl = CELEBRUSCSA_collectionUrl_UAT;
                break;
            default:
                collectionUrl = CELEBRUSCSA_collectionUrl_UAT;
                break;
        }

        try{
//            Celebrus.stop(false);

            sessionSharing(activity);

            //初始化CSA
            Celebrus.start(UseCase.MARKETING,
                    Communications.REAL_TIME,
                    OperationalMode.LIVE,
                    CELEBRUSCSA_csaName,
                    collectionUrl,
                    activity,
                    false,
                    true,
                    CELEBRUSCSA_appName);

//            Log.i("CelebrusCSAUtil",
//                    "start: "+activity.getClass().getName()
//                            + " csaName: "+CELEBRUSCSA_csaName
//                            + " collectionUrl: "+CELEBRUSCSA_collectionUrl
//                            + " appName: "+CELEBRUSCSA_appName);
//
//            Toast.makeText(activity, "CelebrusCSAUtil "
//                    +"start: "+activity.getClass().getName()
//                            + " csaName: "+CELEBRUSCSA_csaName
//                            + " collectionUrl: "+CELEBRUSCSA_collectionUrl
//                            + " appName: "+CELEBRUSCSA_appName, Toast.LENGTH_LONG).show();
        }catch (Exception e){}
    }

    public static void instrument(Application application){
        AutoInstrumentApiLevel14.instrument(application, InstrumentationOption.WEBVIEW_SESSION_SHARING,InstrumentationOption.ORIENTATION);
//        Log.i("CelebrusCSAUtil", "instrument: "+application.getClass().getName());
//        Toast.makeText(application, "CelebrusCSAUtil "+ "instrument: "+application.getClass().getName(), Toast.LENGTH_LONG).show();
    }

    public static void sessionSharing(Activity activity){
        //設定分享資訊
        Celebrus.setAppAndWebViewsShareSession(true);
        //設定要分享device identifier的網域
        Celebrus.shareDeviceIdWithWebViewDomains(domains);

//        Log.i("CelebrusCSAUtil", "sessionSharing: "+activity.getClass().getName()+ " domains:"+ domains);
//        Toast.makeText(activity, "CelebrusCSAUtil "+ "sessionSharing: "+activity.getClass().getName()+ " domains:"+ domains, Toast.LENGTH_LONG).show();
    }

    public static void stop(){
        Celebrus.stop(false);
    }
}
