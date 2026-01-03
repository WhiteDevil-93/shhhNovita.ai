package com.stormyai.app.presentation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stormyai.app.common.MAX_DIMENSION
import com.stormyai.app.common.MIN_DIMENSION
import com.stormyai.app.common.RemixStore
import com.stormyai.app.domain.model.AiModel
import com.stormyai.app.domain.model.ModelProfile
import com.stormyai.app.domain.usecase.CreateImageUseCase
import com.stormyai.app.domain.usecase.GetModelsUseCase
import com.stormyai.app.domain.usecase.GetProfilesUseCase
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
    val profiles: List<ModelProfile> = emptyList(),
    val selectedProfile: ModelProfile? = null,
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 30,
    val cfgScale: Float = 7.0f,
    val sampler: String = "DPM++ SDE Karras",
    val imageCount: Int = 1,
    val seedMode: SeedMode = SeedMode.RANDOM,
    val seed: String = "",
    val highResFix: Boolean = false,
    val faceRestore: Boolean = false,
    val nsfw: Boolean = false,
    val loraName: String = "",
    val loraWeight: String = "0.7",
    val loras: List<com.stormyai.app.domain.model.Lora> = emptyList(),
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val status: GenerationStatus = GenerationStatus.IDLE,
    val error: String? = null
)

enum class SeedMode {
    RANDOM,
    FIXED
}

enum class GenerationStatus {
    IDLE,
    QUEUED,
    GENERATING,
    UPSCALING,
    DONE,
    FAILED
}

@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val createImageUseCase: CreateImageUseCase,
    private val pollTaskStatusUseCase: PollTaskStatusUseCase,
    private val saveToHistoryUseCase: SaveToHistoryUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val getProfilesUseCase: GetProfilesUseCase
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = mutableUiState.asStateFlow()

    init {
        loadModels()
        observeRemix()
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

    fun updateSelectedProfile(profile: ModelProfile) {
        mutableUiState.value = uiState.value.copy(selectedProfile = profile, error = null)
    }

    fun updateSampler(value: String) {
        mutableUiState.value = uiState.value.copy(sampler = value)
    }

    fun updateImageCount(value: Int) {
        val clamped = value.coerceIn(1, 8)
        mutableUiState.value = uiState.value.copy(imageCount = clamped)
    }

    fun updateSeedMode(mode: SeedMode) {
        mutableUiState.value = uiState.value.copy(seedMode = mode)
    }

    fun updateSeed(value: String) {
        mutableUiState.value = uiState.value.copy(seed = value)
    }

    fun updateHighResFix(value: Boolean) {
        mutableUiState.value = uiState.value.copy(highResFix = value)
    }

    fun updateFaceRestore(value: Boolean) {
        mutableUiState.value = uiState.value.copy(faceRestore = value)
    }

    fun updateNsfw(value: Boolean) {
        val current = uiState.value
        if (value && current.selectedProfile?.nsfwAllowed == false) {
            mutableUiState.value = current.copy(
                error = "Selected profile does not allow NSFW mode",
                nsfw = false
            )
            return
        }
        mutableUiState.value = current.copy(nsfw = value, error = null)
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

        mutableUiState.value = current.copy(
            isGenerating = true,
            status = GenerationStatus.QUEUED,
            error = null
        )
        viewModelScope.launch {
            mutableUiState.value = uiState.value.copy(status = GenerationStatus.GENERATING)
            val seed = if (current.seedMode == SeedMode.FIXED) {
                current.seed.toLongOrNull()
            } else {
                null
            }
            val result = createImageUseCase(
                prompt = current.prompt,
                negativePrompt = current.negativePrompt.ifBlank { null },
                width = current.width,
                height = current.height,
                steps = current.steps,
                cfgScale = current.cfgScale,
                sampler = current.sampler,
                imageCount = current.imageCount,
                seed = seed,
                highResFix = current.highResFix,
                faceRestore = current.faceRestore,
                nsfw = current.nsfw,
                profile = current.selectedProfile,
                modelId = current.selectedModel?.id,
                loras = current.loras
            )
            if (result.isSuccess) {
                mutableUiState.value = uiState.value.copy(
                    isGenerating = false,
                    status = GenerationStatus.DONE
                )
            } else {
                mutableUiState.value = uiState.value.copy(
                    isGenerating = false,
                    status = GenerationStatus.FAILED,
                    error = result.exceptionOrNull()?.message ?: "Generation failed"
                )
            }
        }
    }

    private fun loadModels() {
        viewModelScope.launch {
            val result = getModelsUseCase()
            val models = result.getOrDefault(emptyList())
            val profiles = getProfilesUseCase().getOrDefault(emptyList())
            mutableUiState.value = uiState.value.copy(
                models = models,
                selectedModel = models.firstOrNull(),
                profiles = profiles,
                selectedProfile = profiles.firstOrNull(),
                isLoading = false
            )
        }
    }

    private fun observeRemix() {
        viewModelScope.launch {
            RemixStore.item.collect { item ->
                if (item != null) {
                    mutableUiState.value = uiState.value.copy(
                        prompt = item.prompt,
                        negativePrompt = item.negativePrompt.orEmpty(),
                        steps = item.steps,
                        cfgScale = item.cfgScale,
                        sampler = item.sampler,
                        imageCount = item.imageCount,
                        seedMode = if (item.seed == null) SeedMode.RANDOM else SeedMode.FIXED,
                        seed = item.seed?.toString().orEmpty(),
                        highResFix = item.highResFix,
                        faceRestore = item.faceRestore,
                        nsfw = item.nsfw
                    )
                    RemixStore.clear()
                }
            }
        }
    }
}
