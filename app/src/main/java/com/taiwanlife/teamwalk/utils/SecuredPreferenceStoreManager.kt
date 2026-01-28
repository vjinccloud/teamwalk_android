package com.taiwanlife.teamwalk.utils

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.taiwanlife.teamwalk.MyApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object SecuredPreferenceStoreManager {
    private const val KEYSET_NAME = "teamwalk2_master_keyset"
    private const val PREF_NAME = "secure_teamwalk2_tink_keyset_prefs"
    private const val MASTER_KEY_URI = "android-keystore://teamwalk2_tink_master_key"

    private lateinit var dataStore: DataStore<Preferences>
    private val aead: Aead by lazy {
        val keysetHandle =
            AndroidKeysetManager.Builder()
                .withSharedPref(
                    MyApplication.context,
                    KEYSET_NAME,
                    PREF_NAME
                )
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle

        keysetHandle.getPrimitive(
            RegistryConfiguration.get(),
            Aead::class.java
        )
    }

    fun init(context: Context) {
        TinkConfig.register()

        AndroidKeysetManager.Builder()
            .withSharedPref(
                context,
                KEYSET_NAME,
                PREF_NAME
            )
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()

        dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile(PREF_NAME)
            }
        )
    }

    fun editAndApply(callback: (SecureEditor) -> Unit) {
//        val editor = SecureEditor(prefs.edit())
        val editor = SecureEditor()
        callback(editor)
        editor.apply()
    }

    fun simpleEditAndApply(key: String, value: String) {
        val encrypted = encrypt(value)
        runBlocking {
            dataStore.edit {
                it[stringPreferencesKey(key)] = encrypted
            }
        }
    }

    fun getString(key: String, defaultValue: String): String {
        val encrypted = runBlocking {
            dataStore.data.first()[stringPreferencesKey(key)]
        } ?: return defaultValue

        return decrypt(encrypted) ?: defaultValue
    }

//    fun simpleEditAndApply(key: String, value: String) {
//        val encrypted = encrypt(value)
//        prefs.edit { putString(key, encrypted) }
//    }
//
//    fun getString(key: String, defaultValue: String): String {
//        val encrypted = prefs.getString(key, null) ?: return defaultValue
//        val decrypted = decrypt(encrypted) ?: defaultValue
//        return decrypted
//    }

    fun getInt(key: String, defaultValue: Int): Int {
        return getString(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue
    }

    private fun encrypt(plainText: String): String {
        val cipher = aead.encrypt(
            plainText.toByteArray(Charsets.UTF_8),
            null
        )
        return Base64.encodeToString(cipher, Base64.NO_WRAP)
    }

    private fun decrypt(cipherTextBase64: String): String? {
        return try {
            val cipher = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val plain = aead.decrypt(cipher, null)
            String(plain, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class SecureEditor {

        private val pending = mutableMapOf<String, String?>()

        fun putString(key: String, value: String): SecureEditor {
            pending[key] = encrypt(value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): SecureEditor {
            pending[key] = encrypt(value.toString())
            return this
        }

        fun putInt(key: String, value: Int): SecureEditor {
            pending[key] = encrypt(value.toString())
            return this
        }

        fun remove(key: String): SecureEditor {
            pending[key] = null
            return this
        }

        fun apply() {
            runBlocking {
                dataStore.edit { prefs ->
                    pending.forEach { (k, v) ->
                        val prefKey = stringPreferencesKey(k)
                        if (v == null) prefs.remove(prefKey)
                        else prefs[prefKey] = v
                    }
                }
            }
        }
    }
}