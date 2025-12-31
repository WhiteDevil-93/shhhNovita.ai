package com.novitaai.studio.presentation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novitaai.studio.domain.model.AiModel
import com.novitaai.studio.domain.model.GenerationResult
import com.novitaai.studio.domain.model.GenerationType
import com.novitaai.studio.domain.model.ModelType
import com.novitaai.studio.domain.usecase.CreateImageUseCase
import com.novitaai.studio.domain.usecase.GetModelsUseCase
import com.novitaai.studio.domain.usecase.PollTaskStatusUseCase
import com.novitaai.studio.domain.usecase.SaveToHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Generate screen
 */
data class GenerateUiState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedModel: AiModel? = null,
    val width: Int = 512,
    val height: Int = 768,
    val steps: Int = 25,
    val cfgScale: Float = 7.0f,
    val seed: Int? = null,
    val generationType: GenerationType = GenerationType.TEXT_TO_IMAGE,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val progress: Float = 0f,
    val currentResult: GenerationResult? = null,
    val error: String? = null,
    val models: List<AiModel> = emptyList(),
    val showModelSelector: Boolean = false,
    val showAdvancedSettings: Boolean = false
)

/**
 * ViewModel for the Generate screen
 */
@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val createImageUseCase: CreateImageUseCase,
    private val pollTaskStatusUseCase: PollTaskStatusUseCase,
    private val saveToHistoryUseCase: SaveToHistoryUseCase,
    private val getModelsUseCase: GetModelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getModelsUseCase().fold(
                onSuccess = { models ->
                    _uiState.update { state ->
                        state.copy(
                            models = models,
                            selectedModel = models.firstOrNull { it.isRecommended }
                                ?: models.firstOrNull(),
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            )
        }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt, error = null) }
    }

    fun updateNegativePrompt(negativePrompt: String) {
        _uiState.update { it.copy(negativePrompt = negativePrompt) }
    }

    fun updateModel(model: AiModel) {
        _uiState.update { it.copy(selectedModel = model, showModelSelector = false) }
    }

    fun updateWidth(width: Int) {
        _uiState.update { it.copy(width = width.coerceIn(256, 2048)) }
    }

    fun updateHeight(height: Int) {
        _uiState.update { it.copy(height = height.coerceIn(256, 2048)) }
    }

    fun updateSteps(steps: Int) {
        _uiState.update { it.copy(steps = steps.coerceIn(1, 100)) }
    }

    fun updateCfgScale(scale: Float) {
        _uiState.update { it.copy(cfgScale = scale.coerceIn(1.0f, 20.0f)) }
    }

    fun updateSeed(seed: Int?) {
        _uiState.update { it.copy(seed = seed) }
    }

    fun updateGenerationType(type: GenerationType) {
        _uiState.update { it.copy(generationType = type) }
    }

    fun toggleModelSelector() {
        _uiState.update { it.copy(showModelSelector = !it.showModelSelector) }
    }

    fun toggleAdvancedSettings() {
        _uiState.update { it.copy(showAdvancedSettings = !it.showAdvancedSettings) }
    }

    fun generate() {
        val state = _uiState.value

        if (state.prompt.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a prompt") }
            return
        }

        if (state.selectedModel == null) {
            _uiState.update { it.copy(error = "Please select a model") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, progress = 0f, error = null) }

            createImageUseCase(
                prompt = state.prompt,
                negativePrompt = state.negativePrompt,
                width = state.width,
                height = state.height,
                steps = state.steps,
                cfgScale = state.cfgScale,
                seed = state.seed
            ).fold(
                onSuccess = { result ->
                    // Start polling for completion
                    pollForResult(result.taskId)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = error.message ?: "Generation failed"
                        )
                    }
                }
            )
        }
    }

    private fun pollForResult(taskId: String) {
        viewModelScope.launch {
            pollTaskStatusUseCase(taskId).fold(
                onSuccess = { result ->
                    // Save to history
                    saveToHistoryUseCase(
                        taskId = result.taskId,
                        type = result.type,
                        prompt = _uiState.value.prompt,
                        resultUrl = result.imageUrl ?: result.videoUrl,
                        modelName = _uiState.value.selectedModel?.name ?: "unknown"
                    )

                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            currentResult = result,
                            progress = 1f
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = error.message ?: "Polling failed"
                        )
                    }
                }
            )
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(currentResult = null, progress = 0f) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun reset() {
        _uiState.update {
            it.copy(
                prompt = "",
                negativePrompt = "",
                currentResult = null,
                progress = 0f,
                error = null
            )
        }
    }
}
