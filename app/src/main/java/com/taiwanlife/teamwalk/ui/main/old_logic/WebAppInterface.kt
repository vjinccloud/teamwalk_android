package com.taiwanlife.teamwalk.ui.main.old_logic

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.taiwanlife.teamwalk.BuildConfig
import com.taiwanlife.teamwalk.Config
import com.taiwanlife.teamwalk.EnvironmentManager
import com.taiwanlife.teamwalk.java_utils.FortifyUtil
import com.taiwanlife.teamwalk.utils.SecuredPreferenceStoreManager
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WebAppInterface(
    private val context: Context,
    private val mainWebViewJavaCallback: MainWebViewJava.MainWebViewJavaCallback
) {

    private val environmentConfig = EnvironmentManager.getEnvironmentConfig()
    private val env = BuildConfig.BUILD_TYPE

    @JavascriptInterface
    fun signInUser(): String? {
        val token = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_TOKEN, "")
        val refreshToken =
            SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_REFRESH_TOKEN, "")
        val exp = SecuredPreferenceStoreManager.getLong(Config.PREF_LOGIN_EXP, 0L)
        val castgc = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_CASTGC, "")
        var uUid: String? = SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_UUID, "")
        //            String token = loginSharedPref.getString(getString(R.string.pref_login_token), "");
//            String refreshToken = loginSharedPref.getString(getString(R.string.pref_login_refresh_token), "");
//            Long exp = loginSharedPref.getLong(getString(R.string.pref_login_exp), 0L);
//            String castgc = loginSharedPref.getString(getString(R.string.pref_login_castgc), "");
//            String uUid = loginSharedPref.getString(getString(R.string.pref_login_uuid), "");
        if (uUid == null || uUid == "") {
//                uUid = fid;
            uUid = mainWebViewJavaCallback.getFid()
            SecuredPreferenceStoreManager.simpleEditAndApply(Config.PREF_LOGIN_UUID, uUid!!)
            //                loginSharedPref.edit().putString(getString(R.string.pref_login_uuid), uUid);
        }
        val user: MutableMap<String?, String?> = HashMap<String?, String?>()
        //            user.put("pid", pid);
        user.put("pid", mainWebViewJavaCallback.getPid())
        //            user.put("ticket", ticket);
        user.put("ticket", mainWebViewJavaCallback.getTicket())
        user.put("token", token)
        user.put("refreshToken", refreshToken)
        user.put("exp", exp.toString())
        user.put("castgc", castgc)
        user.put("fcm", SecuredPreferenceStoreManager.getString(Config.PREF_LOGIN_FID, ""))
        user.put("fid", uUid)
        try {
            val pInfo: PackageInfo =
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            user.put("vNo", pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.Forest.d("Fail to get package version")
        }
        return (Gson()).toJson(user)
    }

    @JavascriptInterface
    fun copyToClipboard(copyText: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("simple text", copyText)
        clipboard.setPrimaryClip(clip)

        //            Toast.makeText(MainActivity.this, R.string.main_add_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    fun bindGoogleFit() {
        // 版本29之後才需要要求此權限 29之前的可以直接執行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissions = ArrayList<String?>()
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            mainWebViewJavaCallback.getPermissionAndCallback(
                permissions,
                MainWebViewJava.SimpleCallback {
                    mainWebViewJavaCallback.accessGoogleFit()
                })
        } else {
            mainWebViewJavaCallback.accessGoogleFit()
        }
        //            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
//                    mainWebViewJavaCallback.accessGoogleFit();
//                } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)) {
//                } else {
//                    ActivityCompat.requestPermissions(getContext(),
//                            new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
//                            PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
//                }
//            } else {
//                mainWebViewJavaCallback.accessGoogleFit();
//            }
    }

    @JavascriptInterface
    fun bindFitbit() {
        val url = "https://www.fitbit.com/oauth2/authorize?" +
                "client_id=" + environmentConfig.connectFitbitClientId + "&" +
                "response_type=code" + "&" +
                "scope=" + "activity%20sleep" + "&" +
                "expires_in=31536000&prompt=login%20consent&redirect_uri=teamwalk" + env + "://webconnect?device=fitbit"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun bindGarmin() {
        mainWebViewJavaCallback.callGetGarminAuthCode()
    }

    @JavascriptInterface
    fun removePattern() {
        SecuredPreferenceStoreManager.editAndApply { editor: SecuredPreferenceStore.Editor? ->
            editor!!.putBoolean(Config.PREF_LOGIN_PATTERN_STATUS, false)
            editor.putInt(Config.PREF_LOGIN_SEGMENT_CONTROL_POS, 0)
            Unit
        }
    }

    @JavascriptInterface
    fun removeGoogleFit() {
        // TODO
        mainWebViewJavaCallback.removeGoogleFit()
        //            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                    .requestEmail()
//                    .build();
//
//            googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
//            startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGN_IN_FOR_DISABLE_FIT);
    }

    @JavascriptInterface
    fun signOut() {
        SecuredPreferenceStoreManager.editAndApply { editor: SecuredPreferenceStore.Editor? ->
            editor!!.putBoolean(Config.PREF_LOGIN_AUTH, false)
            editor.putString(Config.PREF_LOGIN_USERNAME, "")
            editor.putString(Config.PREF_LOGIN_TICKET, "")
            editor.putString(Config.PREF_LOGIN_CASTGC, "")
            editor.putString(Config.PREF_LOGIN_TOKEN, "")
            editor.putString(Config.PREF_LOGIN_REFRESH_TOKEN, "")
            editor.putLong(Config.PREF_LOGIN_EXP, 0L)
            Unit
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("teamwalk" + env + "://login"))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun syncGoogleFit(token: String?) {
        // TODO
//            runOnUiThread(() -> {
//                teamwalkToken = token;
//                Calendar start = Calendar.getInstance();
//                start.add(Calendar.DATE, -6);
//                start.set(Calendar.HOUR_OF_DAY, 0);
//                start.set(Calendar.MINUTE, 0);
//                start.set(Calendar.SECOND, 0);
//                Calendar end = Calendar.getInstance();
//                end.add(Calendar.DATE, 1);
//                end.set(Calendar.HOUR_OF_DAY, 0);
//                end.set(Calendar.MINUTE, 0);
//                end.set(Calendar.SECOND, 0);
//                stepDataMap = new HashMap<>();
//                caloriesDataMap = new HashMap<>();
//                sleepDataList = new ArrayList<>();
//                getStepData(start.getTimeInMillis(), end.getTimeInMillis());
//            });
    }

    @JavascriptInterface
    fun saveDataToFile(data: String, fileName: String): String? {
        if (data.isEmpty() || fileName.isEmpty() || fileName.length <= 0 || FortifyUtil.checkFileName(
                fileName
            )
        ) {
            return null
        } else {
            val savePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getPath()
            var saveFile: String? = ""
            var fileOutputStream: FileOutputStream? = null
            try {
//                fileName = "數位理賠進件同意書.docx";
                val fileBytes =
                    Base64.decode(data.replaceFirst("data:text/xml;base64,".toRegex(), ""), 0)
                fileOutputStream = FileOutputStream(File(savePath, fileName))

                //                fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                fileOutputStream.write(fileBytes)
                //                fileOutputStream.close();
                saveFile = savePath + "/" + fileName
                //            }catch(Exception e){
//                saveFile = null;
//                e.printStackTrace();
//            }
            } catch (ex: IOException) {
                saveFile = null
                //                    ex.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) fileOutputStream.close()
                } catch (e: IOException) {
                    saveFile = null
                    //                        e.printStackTrace();
                }
            }
            return saveFile
        }
    }

    @JavascriptInterface
    fun ShareContent(shareJsonStr: String?): Boolean {
//            if(shareUri!=null)
//                ShareUtil.deleteUri(MainActivity.this, shareUri);
//            shareUri=null;
//            ShareUtil.delShareImage(MainActivity.this);
        return mainWebViewJavaCallback.shareContent(shareJsonStr)
    }

    @JavascriptInterface
    fun runScoreGooglePlay(): Boolean {
        return mainWebViewJavaCallback.scoreGooglePlay()
    }

    @JavascriptInterface
    fun reloadPage() {
//            clearCache = true;
        mainWebViewJavaCallback.setClearCache(true)
    }
}