package com.taiwanlife.teamwalk.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.main.old_logic.SharePopupWindow
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareUtil {
    const val SHAREIMAGE_FILENAME: String = "teamwalk_share_image.png"

    fun shareContent(shareJsonStr: String, activity: Activity): Boolean {
        val retval = false

        try {
            val jsonObject = JSONObject(shareJsonStr)
//            String target=jsonObject.getString("target")
//            String title=jsonObject.getString("title")
//            String content=jsonObject.getString("content")
//            String image=jsonObject.getString("image")
//            String url=jsonObject.getString("url")

            val spw = SharePopupWindow(activity)
            spw.init(activity, shareJsonStr)
            // 显示窗口
            spw.showAtLocation(activity.currentFocus, Gravity.BOTTOM, 0, 0);

        } catch (e: JSONException) {
//            e.printStackTrace();
//            Log.e("shareContent Exception", e.getMessage());
            return false
        }
        return retval
    }

    fun runShareContent(
        title: String,
        content: String,
        image: String,
        url: String,
        appInfo: AppInfo,
        activity: Activity
    ) {
        if (appInfo.appName.uppercase().equals(activity.getString(R.string.shareapp_name_fb))) {
            val errorMsg = activity.getString(R.string.errorstr_fb)
            runActionSend(title, content, image, appInfo, activity, errorMsg)
        } else if (appInfo.appName.uppercase()
                .equals(activity.getString(R.string.shareapp_name_ig))
        ) {
            val errorMsg = activity.getString(R.string.errorstr_ig)
            runActionSend(title, content, image, appInfo, activity, errorMsg)
        } else if (appInfo.appName.uppercase()
                .equals(activity.getString(R.string.shareapp_name_line))
        ) {
            val errorMsg = activity.getString(R.string.errorstr_line)
            runActionSend(title, content, image, appInfo, activity, errorMsg)
        }
    }

    fun runActionSend(
        title: String,
        content: String,
        image: String,
        appInfo: AppInfo,
        activity: Activity,
        errorMsg: String
    ): Boolean {
        var uri: Uri? = null
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.component = ComponentName(
                appInfo.pkgName,
                appInfo.launchClassName
            )
            val bitmap = stringToBitmap(image)
            if (bitmap == null) return false

            saveShareImage(activity, bitmap)
            uri = getShareImageUri(activity)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
//            intent.putExtra(Intent.EXTRA_SUBJECT, content)
//            intent.putExtra(Intent.EXTRA_TITLE, content)
//            intent.putExtra(Intent.EXTRA_TEXT, content)
//            intent.putExtra("sms_body", content)
//            intent.putExtra("Kdescription", content)
            intent.setClassName(appInfo.pkgName, appInfo.launchClassName)
            // 根據以前的程式碼 拿到Result之後也什麼都沒有做 使用正常startActivity就好
//            activity.startActivityForResult(intent, SHARECONTENT_REQUEST)
            activity.startActivity(intent)

        } catch (e: IllegalArgumentException) {
//            Log.e("shareContent Exception", e.getMessage())
            e.printStackTrace()
            Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show()
        } catch (e: NullPointerException) {
            e.printStackTrace()
//            Log.e("shareContent Exception", e.getMessage())
//            Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
//            Log.e("shareContent Exception", e.getMessage())
            e.printStackTrace()
            Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show()
        }
        return true
    }

    fun getShareDefaultAppList(context: Context): List<AppInfo> {
        val shareAppInfos = ArrayList<AppInfo>()
        ContextCompat.getDrawable(context, R.mipmap.fb_group_16_3x)?.let {
            shareAppInfos.add(
                AppInfo(
                    context.getString(R.string.shareapp_name_fb),
                    context.getString(R.string.shareapp_pkgname_fb),
                    context.getString(R.string.shareapp_classname_fb),
                    it,
                )
            )
        }
        ContextCompat.getDrawable(context, R.mipmap.ins_group_17_3x)?.let {
            shareAppInfos.add(
                AppInfo(
                    context.getString(R.string.shareapp_name_ig),
                    context.getString(R.string.shareapp_pkgname_ig),
                    context.getString(R.string.shareapp_classname_ig),
                    it,
                )
            )
        }
        ContextCompat.getDrawable(context, R.mipmap.line_group_14_3x)?.let {
            shareAppInfos.add(
                AppInfo(
                    context.getString(R.string.shareapp_name_line),
                    context.getString(R.string.shareapp_pkgname_line),
                    context.getString(R.string.shareapp_classname_line),
                    it,
                )
            )
        }

        return shareAppInfos
    }

    fun getShareImageFullPath(context: Context): String? {
        return context.getExternalFilesDir(null)?.absolutePath
    }

    fun getShareImageUri(context: Context): Uri? {
        val fullName = getShareImageFullPath(context) + '/' + SHAREIMAGE_FILENAME
        return if (fileIsExist(fullName)) {
            FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                File(fullName)
            )
        } else {
            null
        }
    }

    fun saveShareImage(context: Context, bm: Bitmap) {
        val fullPath = getShareImageFullPath(context)
        delete("$fullPath/$SHAREIMAGE_FILENAME")
        saveBitmap(context, bm, fullPath, SHAREIMAGE_FILENAME)
    }

    fun delShareImage(context: Context) {
        val fullname = getShareImageFullPath(context) + '/' + SHAREIMAGE_FILENAME
        delete(fullname)
    }

    fun stringToBitmap(string: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bitmapArray =
                Base64.decode(string.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1], Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun saveBitmap(context: Context, bm: Bitmap, fullPath: String?, fileName: String) {
        var saveImgOut: FileOutputStream? = null
        try {
            val saveFile = File(fullPath, fileName)

            if (!saveFile.exists()) {
                saveFile.parentFile.mkdirs()
                saveFile.createNewFile()
            }

            saveImgOut = FileOutputStream(saveFile)

            if (bm.compress(Bitmap.CompressFormat.PNG, 90, saveImgOut)) {
                saveImgOut.flush()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            try {
                saveImgOut?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun fileIsExist(fullName: String): Boolean {
        val file = File(fullName)
        return if (file.exists()) true
        else {
            file.mkdirs()
        }
    }

    /** 删除文件，可以是文件或文件夹
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    private fun delete(delFile: String): Boolean {
        val file = File(delFile)
        return if (!file.exists()) {
            false
        } else {
            if (file.isFile) {
                deleteSingleFile(delFile)
            } else {
                deleteDirectory(delFile)
            }
        }
    }

    /** 删除单个文件
     * @param filePathName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private fun deleteSingleFile(filePathName: String): Boolean {
        val file = File(filePathName)
        return if (file.exists() && file.isFile) {
            file.delete()
        } else {
            false
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private fun deleteDirectory(filePath: String?): Boolean {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符

        var filePath = filePath
        if (filePath == null || filePath.isEmpty() || !filePath.endsWith(File.separator.toString())) {
            filePath = filePath + File.separator
        } else return false

        val dirFile = File(filePath)
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory)) {
            return false
        }
        var flag = true
        // 删除文件夹中的所有文件包括子目录
        val files = dirFile.listFiles()
        for (file in files!!) {
            // 删除子文件
            if (file.isFile) {
                flag = deleteSingleFile(file.absolutePath)
                if (!flag) {
                    break
                }
            } else if (file.isDirectory) {
                flag = deleteDirectory(file.absolutePath)
                if (!flag) {
                    break
                }
            }
        }
        if (!flag) {
            return false
        }
        // 删除当前目录
        return if (dirFile.delete()) {
            true
        } else {
            false
        }
    }
}