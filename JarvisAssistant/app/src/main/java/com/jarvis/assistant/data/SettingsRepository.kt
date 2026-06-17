package com.jarvis.assistant.data

import android.content.Context

/**
 * Simple SharedPreferences-backed settings store.
 *
 * NOTE: the API key is stored in plain SharedPreferences for this MVP so
 * the first build is as dependency-light as possible. Before shipping
 * this beyond your own device, switch to androidx.security:security-crypto
 * (EncryptedSharedPreferences) to store the key encrypted at rest.
 */
class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    fun getVaultUri(): String? = prefs.getString(KEY_VAULT_URI, null)
    fun setVaultUri(uri: String) = prefs.edit().putString(KEY_VAULT_URI, uri).apply()

    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, null)
    fun setApiKey(key: String) = prefs.edit().putString(KEY_API_KEY, key).apply()

    fun getScheduleHour(): Int = prefs.getInt(KEY_HOUR, 9)
    fun getScheduleMinute(): Int = prefs.getInt(KEY_MINUTE, 0)
    fun setSchedule(hour: Int, minute: Int) =
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()

    fun getLastRunResult(): String? = prefs.getString(KEY_LAST_RESULT, null)
    fun setLastRunResult(text: String) = prefs.edit().putString(KEY_LAST_RESULT, text).apply()

    companion object {
        private const val KEY_VAULT_URI = "vault_uri"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_HOUR = "schedule_hour"
        private const val KEY_MINUTE = "schedule_minute"
        private const val KEY_LAST_RESULT = "last_run_result"
    }
}
