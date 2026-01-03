package com.stormyai.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import com.stormyai.app.common.DEFAULT_HEIGHT
import com.stormyai.app.common.DEFAULT_WIDTH
import com.stormyai.app.domain.model.UserSettings
import com.stormyai.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings> {
        return dataStore.data.map { preferences ->
            UserSettings(
                apiKey = preferences[API_KEY],
                defaultModelId = preferences[DEFAULT_MODEL_ID],
                defaultWidth = preferences[DEFAULT_WIDTH] ?: DEFAULT_WIDTH,
                defaultHeight = preferences[DEFAULT_HEIGHT] ?: DEFAULT_HEIGHT,
                saveHistory = preferences[SAVE_HISTORY] ?: true
            )
        }
    }

    override suspend fun updateSettings(settings: UserSettings) {
        dataStore.edit { prefs ->
            if (settings.apiKey.isNullOrBlank()) {
                prefs.remove(API_KEY)
            } else {
                prefs[API_KEY] = settings.apiKey
            }
            if (settings.defaultModelId.isNullOrBlank()) {
                prefs.remove(DEFAULT_MODEL_ID)
            } else {
                prefs[DEFAULT_MODEL_ID] = settings.defaultModelId
            }
            prefs[DEFAULT_WIDTH] = settings.defaultWidth
            prefs[DEFAULT_HEIGHT] = settings.defaultHeight
            prefs[SAVE_HISTORY] = settings.saveHistory
        }
    }

    private companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val DEFAULT_MODEL_ID = stringPreferencesKey("default_model_id")
        val DEFAULT_WIDTH = intPreferencesKey("default_width")
        val DEFAULT_HEIGHT = intPreferencesKey("default_height")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
    }
}
