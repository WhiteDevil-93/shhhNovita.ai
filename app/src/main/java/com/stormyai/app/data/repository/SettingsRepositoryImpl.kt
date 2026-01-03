package com.stormyai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.stormyai.app.common.DEFAULT_HEIGHT
import com.stormyai.app.common.DEFAULT_WIDTH
import com.stormyai.app.domain.model.UserSettings
import com.stormyai.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    context: Context
) : SettingsRepository {

    // Use EncryptedSharedPreferences for API key storage
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun getSettings(): Flow<UserSettings> {
        return dataStore.data.map { preferences ->
            UserSettings(
                apiKey = encryptedPrefs.getString(ENCRYPTED_API_KEY, null),
                defaultModelId = preferences[DEFAULT_MODEL_ID],
                defaultSampler = preferences[DEFAULT_SAMPLER] ?: DEFAULT_SAMPLER_VALUE,
                defaultWidth = preferences[DEFAULT_WIDTH] ?: DEFAULT_WIDTH,
                defaultHeight = preferences[DEFAULT_HEIGHT] ?: DEFAULT_HEIGHT,
                defaultSteps = preferences[DEFAULT_STEPS] ?: DEFAULT_STEPS_VALUE,
                defaultCfgScale = preferences[DEFAULT_CFG_SCALE] ?: DEFAULT_CFG_SCALE_VALUE,
                saveHistory = preferences[SAVE_HISTORY] ?: true
            )
        }
    }

    override suspend fun updateSettings(settings: UserSettings) {
        // Store API key in encrypted preferences
        with(encryptedPrefs.edit()) {
            if (settings.apiKey.isNullOrBlank()) {
                remove(ENCRYPTED_API_KEY)
            } else {
                putString(ENCRYPTED_API_KEY, settings.apiKey)
            }
            apply()
        }

        // Store other settings in DataStore
        dataStore.edit { prefs ->
            if (settings.defaultModelId.isNullOrBlank()) {
                prefs.remove(DEFAULT_MODEL_ID)
            } else {
                prefs[DEFAULT_MODEL_ID] = settings.defaultModelId
            }
            prefs[DEFAULT_SAMPLER] = settings.defaultSampler
            prefs[DEFAULT_WIDTH] = settings.defaultWidth
            prefs[DEFAULT_HEIGHT] = settings.defaultHeight
            prefs[DEFAULT_STEPS] = settings.defaultSteps
            prefs[DEFAULT_CFG_SCALE] = settings.defaultCfgScale
            prefs[SAVE_HISTORY] = settings.saveHistory
        }
    }

    private companion object {
        const val ENCRYPTED_API_KEY = "api_key"
        val DEFAULT_MODEL_ID = stringPreferencesKey("default_model_id")
        val DEFAULT_SAMPLER = stringPreferencesKey("default_sampler")
        val DEFAULT_WIDTH = intPreferencesKey("default_width")
        val DEFAULT_HEIGHT = intPreferencesKey("default_height")
        val DEFAULT_STEPS = intPreferencesKey("default_steps")
        val DEFAULT_CFG_SCALE = floatPreferencesKey("default_cfg_scale")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")

        const val DEFAULT_SAMPLER_VALUE = "DPM++ SDE Karras"
        const val DEFAULT_STEPS_VALUE = 30
        const val DEFAULT_CFG_SCALE_VALUE = 7.0f
    }
}
