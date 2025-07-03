package com.taiwanlife.teamwalk.utils

import android.content.SharedPreferences
import devliving.online.securedpreferencestore.SecuredPreferenceStore

/**
 * 以前的專案就有在使用的函示庫 看起來就是將SharedPreference做AES的Wrapper
 * 網址: https://github.com/iamMehedi/Secured-Preference-Store
 */
object SecuredPreferenceStoreManager {
    private val securedPreferenceStore = SecuredPreferenceStore.getSharedInstance()

    fun editAndApply(editCallback: (SecuredPreferenceStore.Editor) -> Unit) {
        val editor = securedPreferenceStore.edit()
        editCallback(editor)
        editor.apply()
    }

    fun simpleEditAndApply(key: String, value: String) {
        val editor = securedPreferenceStore.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return securedPreferenceStore.getString(key, defaultValue)!!
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return securedPreferenceStore.getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return securedPreferenceStore.getLong(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return securedPreferenceStore.getFloat(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return securedPreferenceStore.getBoolean(key, defaultValue)
    }
}