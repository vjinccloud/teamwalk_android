package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.core.content.edit

object SecuredPreferenceStoreManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "secured_pref_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128
    private const val PREF_NAME = "secure_teamwalk2_prefs"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        initKeyIfNeeded()
    }

    fun editAndApply(callback: (SecureEditor) -> Unit) {
        val editor = SecureEditor(prefs.edit())
        callback(editor)
        editor.apply()
    }

    fun simpleEditAndApply(key: String, value: String) {
        val encrypted = encrypt(value)
        prefs.edit { putString(key, encrypted) }
    }

    fun getString(key: String, defaultValue: String): String {
        val encrypted = prefs.getString(key, null) ?: return defaultValue
        val decrypted = decrypt(encrypted) ?: defaultValue
        return decrypted
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return getString(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return getString(key, defaultValue.toString()).toFloatOrNull() ?: defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue
    }


    private fun initKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator
                .getInstance("AES", ANDROID_KEYSTORE)

            val spec = android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                // .setUserAuthenticationRequired(true)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(cipherTextBase64: String): String? {
        return try {
            val bytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            if (bytes.size <= IV_SIZE) return null

            val iv = bytes.copyOfRange(0, IV_SIZE)
            val encrypted = bytes.copyOfRange(IV_SIZE, bytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(TAG_SIZE, iv))
            val decoded = cipher.doFinal(encrypted)

            String(decoded, Charsets.UTF_8)

        } catch (e: Exception) {
            null
        }
    }

    class SecureEditor(private val editor: SharedPreferences.Editor) {

        fun putString(key: String, value: String): SecureEditor {
            editor.putString(key, encrypt(value))
            return this
        }

        fun putBoolean(key: String, value: Boolean): SecureEditor {
            editor.putString(key, encrypt(value.toString()))
            return this
        }

        fun putInt(key: String, value: Int): SecureEditor {
            editor.putString(key, encrypt(value.toString()))
            return this
        }

        fun putLong(key: String, value: Long): SecureEditor {
            editor.putString(key, encrypt(value.toString()))
            return this
        }

        fun putFloat(key: String, value: Float): SecureEditor {
            editor.putString(key, encrypt(value.toString()))
            return this
        }

        fun remove(key: String): SecureEditor {
            editor.remove(key); return this
        }

        fun clear(): SecureEditor {
            editor.clear(); return this
        }

        fun apply() = editor.apply()
    }
}