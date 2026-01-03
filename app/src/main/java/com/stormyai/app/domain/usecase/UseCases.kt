package com.stormyai.app.domain.usecase

import com.stormyai.app.domain.model.*
import com.stormyai.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CreateImageUseCase(
    private val generationRepository: GenerationRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(
        prompt: String,
        negativePrompt: String? = null,
        width: Int? = null,
        height: Int? = null,
        steps: Int? = null,
        cfgScale: Float? = null,
        modelId: String? = null,
        loras: List<Lora> = emptyList()
    ): Result<GenerationResult> {
        val settings = settingsRepository.getSettings().first()
        val task = GenerationTask(
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width ?: settings.defaultWidth,
            height = height ?: settings.defaultHeight,
            steps = steps ?: settings.defaultSteps,
            cfgScale = cfgScale ?: settings.defaultCfgScale,
            modelId = modelId ?: settings.defaultModelId,
            loras = loras,
            type = GenerationType.TEXT_TO_IMAGE
        )
        val result = generationRepository.generateImage(task)
        val generated = result.getOrNull()
        if (generated != null && settings.saveHistory) {
            historyRepository.saveHistoryItem(
                HistoryItem(
                    taskId = generated.taskId,
                    type = generated.type,
                    prompt = prompt,
                    thumbnailUrl = generated.imageUrl.orEmpty(),
                    resultUrl = generated.imageUrl.orEmpty(),
                    modelName = settings.defaultModelId ?: "",
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        return result
    }
}

class SaveToHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(
        taskId: String,
        type: GenerationType,
        prompt: String,
        resultUrl: String,
        modelName: String
    ): Long {
        val item = HistoryItem(
            taskId = taskId,
            type = type,
            prompt = prompt,
            thumbnailUrl = resultUrl,
            resultUrl = resultUrl,
            modelName = modelName,
            createdAt = System.currentTimeMillis()
        )
        return historyRepository.saveHistoryItem(item)
    }
}

class GetHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<HistoryItem>> {
        return historyRepository.getAllHistory()
    }
}

class PollTaskStatusUseCase(
    private val generationRepository: GenerationRepository
) {
    suspend operator fun invoke(taskId: String): Result<TaskStatus> {
        return generationRepository.pollTaskStatus(taskId)
    }
}

class GetModelsUseCase(
    private val generationRepository: GenerationRepository
) {
    operator fun invoke(): Result<List<AiModel>> {
        return generationRepository.getModels()
    }
}

class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings) {
        settingsRepository.updateSettings(settings)
    }
}
