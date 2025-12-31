package com.novitaai.studio.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.novitaai.studio.domain.model.UserSettings
import com.novitaai.studio.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "novita_settings")

/**
 * Implementation of SettingsRepository using DataStore
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val API_KEY = stringPreferencesKey("api_key")
        val DEFAULT_MODEL = stringPreferencesKey("default_model")
        val DEFAULT_WIDTH = intPreferencesKey("default_width")
        val DEFAULT_HEIGHT = intPreferencesKey("default_height")
        val DEFAULT_STEPS = intPreferencesKey("default_steps")
        val DEFAULT_CFG_SCALE = floatPreferencesKey("default_cfg_scale")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
        val AUTO_DOWNLOAD = booleanPreferencesKey("auto_download")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    override fun getApiKey(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.API_KEY] ?: ""
        }
    }

    override suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = apiKey
        }
    }

    override suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.API_KEY)
        }
    }

    override fun getSettings(): Flow<UserSettings> {
        return context.dataStore.data.map { preferences ->
            UserSettings(
                apiKey = preferences[PreferencesKeys.API_KEY] ?: "",
                defaultModel = preferences[PreferencesKeys.DEFAULT_MODEL] ?: "meinamix_v11",
                defaultWidth = preferences[PreferencesKeys.DEFAULT_WIDTH] ?: 512,
                defaultHeight = preferences[PreferencesKeys.DEFAULT_HEIGHT] ?: 768,
                defaultSteps = preferences[PreferencesKeys.DEFAULT_STEPS] ?: 25,
                defaultCfgScale = preferences[PreferencesKeys.DEFAULT_CFG_SCALE] ?: 7.0f,
                saveHistory = preferences[PreferencesKeys.SAVE_HISTORY] ?: true,
                autoDownload = preferences[PreferencesKeys.AUTO_DOWNLOAD] ?: false,
                darkMode = preferences[PreferencesKeys.DARK_MODE] ?: true
            )
        }
    }

    override suspend fun saveSettings(settings: UserSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = settings.apiKey
            preferences[PreferencesKeys.DEFAULT_MODEL] = settings.defaultModel
            preferences[PreferencesKeys.DEFAULT_WIDTH] = settings.defaultWidth
            preferences[PreferencesKeys.DEFAULT_HEIGHT] = settings.defaultHeight
            preferences[PreferencesKeys.DEFAULT_STEPS] = settings.defaultSteps
            preferences[PreferencesKeys.DEFAULT_CFG_SCALE] = settings.defaultCfgScale
            preferences[PreferencesKeys.SAVE_HISTORY] = settings.saveHistory
            preferences[PreferencesKeys.AUTO_DOWNLOAD] = settings.autoDownload
            preferences[PreferencesKeys.DARK_MODE] = settings.darkMode
        }
    }

    override suspend fun validateApiKey(): Result<Boolean> {
        return try {
            // In a real implementation, this would call the API to validate
            val apiKey = getApiKey().first()
            if (apiKey.isNotBlank() && apiKey.length >= 10) {
                Result.success(true)
            } else {
                Result.failure(Exception("API key not set or invalid"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
