package com.stormyai.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stormyai.app.domain.model.UserSettings
import com.stormyai.app.domain.usecase.UpdateSettingsUseCase
import com.stormyai.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    private val mutableState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                mutableState.value = SettingsUiState(
                    apiKey = settings.apiKey.orEmpty(),
                    defaultModelId = settings.defaultModelId.orEmpty(),
                    defaultWidth = settings.defaultWidth.toString(),
                    defaultHeight = settings.defaultHeight.toString(),
                    saveHistory = settings.saveHistory
                )
            }
        }
    }

    fun updateApiKey(value: String) {
        mutableState.value = state.value.copy(apiKey = value)
    }

    fun updateDefaultModelId(value: String) {
        mutableState.value = state.value.copy(defaultModelId = value)
    }

    fun updateDefaultWidth(value: String) {
        mutableState.value = state.value.copy(defaultWidth = value)
    }

    fun updateDefaultHeight(value: String) {
        mutableState.value = state.value.copy(defaultHeight = value)
    }

    fun updateSaveHistory(value: Boolean) {
        mutableState.value = state.value.copy(saveHistory = value)
    }

    fun saveSettings() {
        val current = state.value
        viewModelScope.launch {
            updateSettingsUseCase(
                UserSettings(
                    apiKey = current.apiKey.ifBlank { null },
                    defaultModelId = current.defaultModelId.ifBlank { null },
                    defaultWidth = current.defaultWidth.toIntOrNull() ?: 512,
                    defaultHeight = current.defaultHeight.toIntOrNull() ?: 512,
                    saveHistory = current.saveHistory
                )
            )
        }
    }
}

data class SettingsUiState(
    val apiKey: String = "",
    val defaultModelId: String = "",
    val defaultWidth: String = "512",
    val defaultHeight: String = "512",
    val saveHistory: Boolean = true
)
