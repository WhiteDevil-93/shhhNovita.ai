package com.novitaai.studio.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novitaai.studio.domain.model.UserSettings
import com.novitaai.studio.domain.repository.SettingsRepository
import com.novitaai.studio.domain.usecase.ValidateApiKeyUseCase
import com.novitaai.studio.domain.usecase.SaveApiKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Settings screen
 */
data class SettingsUiState(
    val apiKey: String = "",
    val defaultModel: String = "meinamix_v11",
    val defaultWidth: Int = 512,
    val defaultHeight: Int = 768,
    val defaultSteps: Int = 25,
    val defaultCfgScale: Float = 7.0f,
    val saveHistory: Boolean = true,
    val autoDownload: Boolean = false,
    val darkMode: Boolean = true,
    val isApiKeyValid: Boolean? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for the Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val validateApiKeyUseCase: ValidateApiKeyUseCase,
    private val saveApiKeyUseCase: SaveApiKeyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            settingsRepository.getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        apiKey = settings.apiKey,
                        defaultModel = settings.defaultModel,
                        defaultWidth = settings.defaultWidth,
                        defaultHeight = settings.defaultHeight,
                        defaultSteps = settings.defaultSteps,
                        defaultCfgScale = settings.defaultCfgScale,
                        saveHistory = settings.saveHistory,
                        autoDownload = settings.autoDownload,
                        darkMode = settings.darkMode,
                        isApiKeyValid = settings.apiKey.isNotBlank(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey, isApiKeyValid = null) }
    }

    fun updateDefaultModel(model: String) {
        _uiState.update { it.copy(defaultModel = model) }
    }

    fun updateDefaultWidth(width: Int) {
        _uiState.update { it.copy(defaultWidth = width.coerceIn(256, 2048)) }
    }

    fun updateDefaultHeight(height: Int) {
        _uiState.update { it.copy(defaultHeight = height.coerceIn(256, 2048)) }
    }

    fun updateDefaultSteps(steps: Int) {
        _uiState.update { it.copy(defaultSteps = steps.coerceIn(1, 100)) }
    }

    fun updateDefaultCfgScale(scale: Float) {
        _uiState.update { it.copy(defaultCfgScale = scale.coerceIn(1.0f, 20.0f)) }
    }

    fun updateSaveHistory(enabled: Boolean) {
        _uiState.update { it.copy(saveHistory = enabled) }
    }

    fun updateAutoDownload(enabled: Boolean) {
        _uiState.update { it.copy(autoDownload = enabled) }
    }

    fun updateDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkMode = enabled) }
    }

    fun validateApiKey() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            validateApiKeyUseCase().fold(
                onSuccess = { isValid ->
                    _uiState.update {
                        it.copy(
                            isApiKeyValid = isValid,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isApiKeyValid = false,
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }

            saveApiKeyUseCase(_uiState.value.apiKey).fold(
                onSuccess = {
                    val settings = UserSettings(
                        apiKey = _uiState.value.apiKey,
                        defaultModel = _uiState.value.defaultModel,
                        defaultWidth = _uiState.value.defaultWidth,
                        defaultHeight = _uiState.value.defaultHeight,
                        defaultSteps = _uiState.value.defaultSteps,
                        defaultCfgScale = _uiState.value.defaultCfgScale,
                        saveHistory = _uiState.value.saveHistory,
                        autoDownload = _uiState.value.autoDownload,
                        darkMode = _uiState.value.darkMode
                    )

                    settingsRepository.saveSettings(settings)

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = "Settings saved successfully"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
