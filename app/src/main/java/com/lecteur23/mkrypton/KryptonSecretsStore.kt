package com.lecteur23.mkrypton

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class KryptonSecrets(
    val telegramApiId: String = "",
    val telegramApiHash: String = "",
    val telegramPhone: String = "",
    val geminiApiKey: String = "",
    val exnessLogin: String = "",
    val exnessPassword: String = "",
    val exnessServer: String = "",
    val demoMode: Boolean = false
)

class KryptonSecretsStore(context: Context) {
    private val appContext = context.applicationContext
    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        appContext,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun load(): KryptonSecrets {
        return KryptonSecrets(
            telegramApiId = prefs.getString(KEY_TELEGRAM_API_ID, "").orEmpty(),
            telegramApiHash = prefs.getString(KEY_TELEGRAM_API_HASH, "").orEmpty(),
            telegramPhone = prefs.getString(KEY_TELEGRAM_PHONE, "").orEmpty(),
            geminiApiKey = prefs.getString(KEY_GEMINI_API_KEY, "").orEmpty(),
            exnessLogin = prefs.getString(KEY_EXNESS_LOGIN, "").orEmpty(),
            exnessPassword = prefs.getString(KEY_EXNESS_PASSWORD, "").orEmpty(),
            exnessServer = prefs.getString(KEY_EXNESS_SERVER, "").orEmpty(),
            demoMode = prefs.getBoolean(KEY_DEMO_MODE, false)
        )
    }

    fun save(secrets: KryptonSecrets) {
        prefs.edit()
            .putString(KEY_TELEGRAM_API_ID, secrets.telegramApiId.trim())
            .putString(KEY_TELEGRAM_API_HASH, secrets.telegramApiHash.trim())
            .putString(KEY_TELEGRAM_PHONE, secrets.telegramPhone.trim())
            .putString(KEY_GEMINI_API_KEY, secrets.geminiApiKey.trim())
            .putString(KEY_EXNESS_LOGIN, secrets.exnessLogin.trim())
            .putString(KEY_EXNESS_PASSWORD, secrets.exnessPassword.trim())
            .putString(KEY_EXNESS_SERVER, secrets.exnessServer.trim())
            .putBoolean(KEY_DEMO_MODE, secrets.demoMode)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "krypton_secure_secrets"
        const val KEY_TELEGRAM_API_ID = "telegram_api_id"
        const val KEY_TELEGRAM_API_HASH = "telegram_api_hash"
        const val KEY_TELEGRAM_PHONE = "telegram_phone"
        const val KEY_GEMINI_API_KEY = "gemini_api_key"
        const val KEY_EXNESS_LOGIN = "exness_login"
        const val KEY_EXNESS_PASSWORD = "exness_password"
        const val KEY_EXNESS_SERVER = "exness_server"
        const val KEY_DEMO_MODE = "demo_mode"
    }
}
