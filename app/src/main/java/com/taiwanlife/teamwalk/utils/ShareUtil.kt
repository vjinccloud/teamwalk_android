package com.taiwanlife.teamwalk.utils

import android.content.Context
import java.io.File

object ShareUtil {
    const val SHAREIMAGE_FILENAME: String = "teamwalk_share_image.png"

    fun getShareImageFullPath(context: Context): String? {
        return context.getExternalFilesDir(null)?.absolutePath
    }

    fun delShareImage(context: Context) {
        val fullname = getShareImageFullPath(context) + '/' + SHAREIMAGE_FILENAME
        delete(fullname)
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