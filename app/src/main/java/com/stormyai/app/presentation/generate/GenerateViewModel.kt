package com.stormyai.app.presentation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stormyai.app.common.MAX_DIMENSION
import com.stormyai.app.common.MIN_DIMENSION
import com.stormyai.app.domain.model.AiModel
import com.stormyai.app.domain.usecase.CreateImageUseCase
import com.stormyai.app.domain.usecase.GetModelsUseCase
import com.stormyai.app.domain.usecase.PollTaskStatusUseCase
import com.stormyai.app.domain.usecase.SaveToHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GenerateUiState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val models: List<AiModel> = emptyList(),
    val selectedModel: AiModel? = null,
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 30,
    val cfgScale: Float = 7.0f,
    val loraName: String = "",
    val loraWeight: String = "0.7",
    val loras: List<com.stormyai.app.domain.model.Lora> = emptyList(),
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val createImageUseCase: CreateImageUseCase,
    private val pollTaskStatusUseCase: PollTaskStatusUseCase,
    private val saveToHistoryUseCase: SaveToHistoryUseCase,
    private val getModelsUseCase: GetModelsUseCase
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = mutableUiState.asStateFlow()

    init {
        loadModels()
    }

    fun updatePrompt(value: String) {
        mutableUiState.value = uiState.value.copy(prompt = value, error = null)
    }

    fun updateNegativePrompt(value: String) {
        mutableUiState.value = uiState.value.copy(negativePrompt = value, error = null)
    }

    fun updateWidth(value: Int) {
        val clamped = value.coerceIn(MIN_DIMENSION, MAX_DIMENSION)
        mutableUiState.value = uiState.value.copy(width = clamped)
    }

    fun updateHeight(value: Int) {
        val clamped = value.coerceIn(MIN_DIMENSION, MAX_DIMENSION)
        mutableUiState.value = uiState.value.copy(height = clamped)
    }

    fun updateSteps(value: Int) {
        val clamped = value.coerceIn(1, 150)
        mutableUiState.value = uiState.value.copy(steps = clamped)
    }

    fun updateCfgScale(value: Float) {
        val clamped = value.coerceIn(1f, 20f)
        mutableUiState.value = uiState.value.copy(cfgScale = clamped)
    }

    fun updateSelectedModel(model: AiModel) {
        mutableUiState.value = uiState.value.copy(selectedModel = model)
    }

    fun updateLoraName(value: String) {
        mutableUiState.value = uiState.value.copy(loraName = value, error = null)
    }

    fun updateLoraWeight(value: String) {
        mutableUiState.value = uiState.value.copy(loraWeight = value, error = null)
    }

    fun addLora() {
        val current = uiState.value
        if (current.loraName.isBlank()) {
            mutableUiState.value = current.copy(error = "Please enter a LoRA name")
            return
        }
        val weight = current.loraWeight.toFloatOrNull()
        if (weight == null) {
            mutableUiState.value = current.copy(error = "LoRA weight must be a number")
            return
        }
        val clamped = weight.coerceIn(0f, 1f)
        val updated = current.loras + com.stormyai.app.domain.model.Lora(
            name = current.loraName.trim(),
            weight = clamped
        )
        mutableUiState.value = current.copy(
            loras = updated,
            loraName = "",
            loraWeight = "0.7",
            error = null
        )
    }

    fun removeLora(index: Int) {
        val current = uiState.value
        if (index !in current.loras.indices) return
        val updated = current.loras.toMutableList().also { it.removeAt(index) }
        mutableUiState.value = current.copy(loras = updated)
    }

    fun generate() {
        val current = uiState.value
        if (current.prompt.isBlank()) {
            mutableUiState.value = current.copy(
                error = "Please enter a prompt",
                isGenerating = false
            )
            return
        }

        mutableUiState.value = current.copy(isGenerating = true, error = null)
        viewModelScope.launch {
            val result = createImageUseCase(
                prompt = current.prompt,
                negativePrompt = current.negativePrompt.ifBlank { null },
                width = current.width,
                height = current.height,
                steps = current.steps,
                cfgScale = current.cfgScale,
                modelId = current.selectedModel?.id,
                loras = current.loras
            )
            if (result.isSuccess) {
                mutableUiState.value = uiState.value.copy(isGenerating = false)
            } else {
                mutableUiState.value = uiState.value.copy(
                    isGenerating = false,
                    error = result.exceptionOrNull()?.message ?: "Generation failed"
                )
            }
        }
    }

    private fun loadModels() {
        viewModelScope.launch {
            val result = getModelsUseCase()
            val models = result.getOrDefault(emptyList())
            mutableUiState.value = uiState.value.copy(
                models = models,
                selectedModel = models.firstOrNull(),
                isLoading = false
            )
        }
    }
}
