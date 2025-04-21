package com.taiwanlife.teamwalk.share;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


import androidx.core.content.FileProvider;

import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ShareUtil {
    public static final String SHAREIMAGE_FILENAME = "teamwalk_share_image.png";
    public static final int SHARECONTENT_REQUEST = 1080;
    public static boolean ShareContent(String shareJsonStr, MainActivity activity) {
        boolean retval =false;

        try {
            JSONObject jsonObject = new JSONObject(shareJsonStr);
//            String target=jsonObject.getString("target");
//            String title=jsonObject.getString("title");
//            String content=jsonObject.getString("content");
//            String image=jsonObject.getString("image");
//            String url=jsonObject.getString("url");

            SharePopupWindow spw = new SharePopupWindow(activity, shareJsonStr);
            // 显示窗口
            spw.showAtLocation(activity.getCurrentFocus(), Gravity.BOTTOM, 0, 0);

        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e("shareContent Exception", e.getMessage());
            return false;
        }
        return retval;
    }

    public static void RunShareContent(String title, String content, String image, String url, AppInfo appInfo, MainActivity activity) {
        String errorMsg = "你還沒安裝 [社群APP名稱] APP喔，要安裝完畢後才能使用本功能！";
        if(appInfo.getAppName().equalsIgnoreCase(activity.getString(R.string.shareapp_name_fb))) {
            errorMsg = activity.getString(R.string.errorstr_fb);
            RunActionSend(title, content, image, url, appInfo, activity, errorMsg);
        }
        else if(appInfo.getAppName().equalsIgnoreCase(activity.getString(R.string.shareapp_name_ig))){
            errorMsg = activity.getString(R.string.errorstr_ig);
            RunActionSend(title, content, image, url, appInfo, activity, errorMsg);
        }
        else if(appInfo.getAppName().equalsIgnoreCase(activity.getString(R.string.shareapp_name_line))){
            errorMsg = activity.getString(R.string.errorstr_line);
            RunActionSend(title, content, image, url, appInfo, activity, errorMsg);
        }
    }
    public static boolean RunActionSend(String title, String content, String image, String url, AppInfo appInfo, MainActivity activity, String errorMsg) {
        boolean retval=false;
        Uri uri = null;
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setComponent(new ComponentName(appInfo.getPkgName(), appInfo.getLaunchClassName()));

            saveShareImage(activity, stringToBitmap(image));
            uri=getShareImageUri(activity);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            intent.putExtra(Intent.EXTRA_SUBJECT, content);
//            intent.putExtra(Intent.EXTRA_TITLE, content);
//            intent.putExtra(Intent.EXTRA_TEXT, content);
//            intent.putExtra("sms_body", content);
//            intent.putExtra("Kdescription", content);
            intent.setClassName(appInfo.getPkgName(), appInfo.getLaunchClassName());
            activity.startActivityForResult(intent, SHARECONTENT_REQUEST);
            retval=true;
        } catch (IllegalArgumentException e) {
//            Log.e("shareContent Exception", e.getMessage());
            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
//        }catch (NullPointerException e) {
//            Log.e("shareContent Exception", e.getMessage());
//            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
        }catch (ActivityNotFoundException e) {
//            Log.e("shareContent Exception", e.getMessage());
            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
        }
        return retval;
    }

    public static ArrayList<AppInfo> getShareDefaultAppList(Context context) {
        ArrayList<AppInfo> shareAppInfos = new ArrayList<AppInfo>();
        AppInfo appInfo = new AppInfo();

        appInfo.setAppName(context.getString(R.string.shareapp_name_fb));
        appInfo.setPkgName(context.getString(R.string.shareapp_pkgname_fb));
        appInfo.setLaunchClassName(context.getString(R.string.shareapp_classname_fb));
        appInfo.setAppIcon(context.getDrawable(R.mipmap.fb_group_16_3x));
        shareAppInfos.add(appInfo);

        appInfo = new AppInfo();
        appInfo.setAppName(context.getString(R.string.shareapp_name_ig));
        appInfo.setPkgName(context.getString(R.string.shareapp_pkgname_ig));
        appInfo.setLaunchClassName(context.getString(R.string.shareapp_classname_ig));
        appInfo.setAppIcon(context.getDrawable(R.mipmap.ins_group_17_3x));
        shareAppInfos.add(appInfo);

        appInfo = new AppInfo();
        appInfo.setAppName(context.getString(R.string.shareapp_name_line));
        appInfo.setPkgName(context.getString(R.string.shareapp_pkgname_line));
        appInfo.setLaunchClassName(context.getString(R.string.shareapp_classname_line));
        appInfo.setAppIcon(context.getDrawable(R.mipmap.line_group_14_3x));
        shareAppInfos.add(appInfo);

        return shareAppInfos;
    }



    public static String getShareImageFullPath(Context context){
        return context.getExternalFilesDir(null).getAbsolutePath();

    }
    public static Uri getShareImageUri(Context context){
        String fullname = getShareImageFullPath(context)+'/'+SHAREIMAGE_FILENAME;
        if (fileIsExist(fullname)){
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    new File(fullname));
        }else {return null;}
    }
    public static void saveShareImage(Context context, Bitmap bm){
        String fullpath = getShareImageFullPath(context);
        delete(fullpath+'/'+SHAREIMAGE_FILENAME);
        saveBitmap(context, bm, fullpath, SHAREIMAGE_FILENAME);
    }
    public static void delShareImage(Context context){
        String fullname = getShareImageFullPath(context)+'/'+SHAREIMAGE_FILENAME;
        delete(fullname);
    }
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
        }
        return bitmap;
    }
    public static void saveBitmap(Context context, Bitmap bm, String fullpath, String filename) {
        FileOutputStream saveImgOut = null;
        try {
            File saveFile = new File(fullpath, filename);

            if (!saveFile.exists()) {
                saveFile.getParentFile().mkdirs();
                saveFile.createNewFile();
            }

            saveImgOut = new FileOutputStream(saveFile);
            // compress - 压缩的意思
            if(bm.compress(Bitmap.CompressFormat.PNG, 90, saveImgOut))
            {
                saveImgOut.flush();
            }
        } catch (IOException ex) {
//            ex.printStackTrace();
        }finally {
            try {
                if(saveImgOut!=null)
                    saveImgOut.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }

        }
    }
    static boolean fileIsExist(String fullName){
        //传入指定的路径，然后判断路径是否存在
        File file=new File(fullName);
        if (file.exists())
            return true;
        else{
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }
    }
    /** 删除文件，可以是文件或文件夹
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    private static boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
//            Toast.makeText(getApplicationContext(), "删除文件失败:" + delFile + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (file.isFile()){
                return deleteSingleFile(delFile);}
            else{
                return deleteDirectory(delFile);}
        }
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
//                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
//                Toast.makeText(getApplicationContext(), "删除单个文件" + filePath$Name + "失败！", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
//            Toast.makeText(getApplicationContext(), "删除单个文件失败：" + filePath$Name + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符

        if (filePath==null || filePath.length()<=0 || !filePath.endsWith(File.separator.toString())){
            filePath = filePath + File.separator;}
        else
            return false;

        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
//            Toast.makeText(getApplicationContext(), "删除目录失败：" + filePath + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag){
                    break;}
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag){
                    break;}
            }
        }
        if (!flag) {
//            Toast.makeText(getApplicationContext(), "删除目录失败！", Toast.LENGTH_SHORT).show();
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
//            Log.e("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
//            Toast.makeText(getApplicationContext(), "删除目录：" + filePath + "失败！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

//    public static boolean RunActionSendMultiple(String title, String content, String image, String url, AppInfo appInfo, MainActivity activity, String errorMsg) {
//        boolean retval=false;
////        Uri uri = null;
//        try {
//            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            intent.setComponent(new ComponentName(appInfo.getPkgName(), appInfo.getLaunchClassName()));
//
//            ArrayList<Uri> uris = new ArrayList<Uri>();
//            saveShareImage(activity, stringToBitmap(image));
//            uris.add(getShareImageUri(activity));
////            uris.add(Uri.parse((String) content));
//            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//            intent.setType("image/*");
//            intent.putExtra(Intent.EXTRA_SUBJECT, content);
//            intent.putExtra(Intent.EXTRA_TITLE, content);
//            intent.putExtra(Intent.EXTRA_TEXT, content);
//            intent.putExtra("sms_body", content);
//            intent.putExtra("Kdescription", content);
//            activity.startActivityForResult(intent, SHARECONTENT_REQUEST);
//            retval=true;
//        } catch (Exception e) {
//            Log.e("shareContent Exception", e.getMessage());
//            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
//        }
//        return retval;
//    }
//    public static void deleteUri(Context context, Uri uri) {
//
//        if (uri.toString().startsWith("content://")) {
//            // content://开头的Uri
//            context.getContentResolver().delete(uri, null, null);
//        } else {
//            File file = new File(getRealFilePath(context,uri));
//            if (file.exists()&& file.isFile()){
//                file.delete();
//            }
//        }
//    }
//    /**
//     * Try to return the absolute file path from the given Uri
//     *
//     * @param context
//     * @param uri
//     * @return the file path or null
//     */
//    public static String getRealFilePath(final Context context, final Uri uri ) {
//        if (null == uri) return null;
//        final String scheme = uri.getScheme();
//        String data = null;
//        if (scheme == null) {
//            data = uri.getPath();
//        }
//        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
//            data = uri.getPath();
//        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
//            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
//            if (null != cursor) {
//                if (cursor.moveToFirst()) {
//                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//                    if (index > -1) {
//                        data = cursor.getString(index);
//                    }
//                }
//                cursor.close();
//            }
//        }
//        return data;
//        }
//    /**
//     * 获取手机内所有支持分享的应用列表
//     */
//    public static ArrayList<AppInfo> getShareAppList(Context context) {
//        ArrayList<AppInfo> shareAppInfos = new ArrayList<AppInfo>();
//        PackageManager packageManager = context.getPackageManager();
//        List<ResolveInfo> resolveInfos = getShareApps(context);
//        if (null == resolveInfos) {
//            return null;
//        } else {
//            for (ResolveInfo resolveInfo : resolveInfos) {
//                AppInfo appInfo = new AppInfo();
//                appInfo.setPkgName(resolveInfo.activityInfo.packageName);
//                appInfo.setLaunchClassName(resolveInfo.activityInfo.name);
//                appInfo.setAppName(resolveInfo.loadLabel(packageManager).toString());
//                appInfo.setAppIcon(resolveInfo.loadIcon(packageManager));
//                if(appInfo.getPkgName().indexOf(".facebook.")!=-1) {
//                    if(appInfo.getLaunchClassName().compareTo("com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias")==0)
//                        shareAppInfos.add(appInfo);
//                }
//                else if(appInfo.getPkgName().indexOf(".line.")!=-1){
//                    if(appInfo.getLaunchClassName().compareTo("com.linecorp.line.share.common.view.FullPickerLaunchActivity")==0)
//                        shareAppInfos.add(appInfo);
//                }
//                else if(appInfo.getPkgName().indexOf(".instagram.")!=-1) {
//                    if(appInfo.getLaunchClassName().compareTo("com.instagram.share.handleractivity.ShareHandlerActivity")==0)
//                        shareAppInfos.add(appInfo);
//                }
////                else if(appInfo.getPkgName().indexOf("com.tencent.mm")!=-1) {
////                    shareAppInfos.add(appInfo);
////                }
//            }
//        }
//        return shareAppInfos;
//    }
//    /**
//     * 查询手机内所有支持分享的应用列表
//     */
//    @SuppressLint("WrongConstant")
//    public static List<ResolveInfo> getShareApps(Context context) {
//        List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
//        Intent intent = new Intent(Intent.ACTION_SEND, null);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        //intent.setType("text/plain"); //纯文本
//        intent.setType("*/*");
//        PackageManager pManager = context.getPackageManager();
//        mApps = pManager.queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
//        return mApps;
//    }

}


