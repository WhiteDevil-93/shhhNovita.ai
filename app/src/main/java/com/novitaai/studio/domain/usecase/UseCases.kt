package com.novitaai.studio.domain.usecase

import com.novitaai.studio.domain.model.*
import com.novitaai.studio.domain.repository.GenerationRepository
import com.novitaai.studio.domain.repository.HistoryRepository
import com.novitaai.studio.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for creating image generation tasks
 */
class CreateImageUseCase @Inject constructor(
    private val generationRepository: GenerationRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(
        prompt: String,
        negativePrompt: String = "",
        width: Int = 512,
        height: Int = 768,
        steps: Int = 25,
        cfgScale: Float = 7.0f,
        seed: Int? = null
    ): Result<GenerationResult> {
        val settings = settingsRepository.getSettings().first()

        val task = GenerationTask(
            id = generateTaskId(),
            type = GenerationType.TEXT_TO_IMAGE,
            prompt = prompt,
            negativePrompt = negativePrompt,
            modelName = settings.defaultModel,
            width = width,
            height = height,
            steps = steps,
            cfgScale = cfgScale,
            seed = seed
        )

        return generationRepository.generateImage(task).also { result ->
            if (result.isSuccess && settings.saveHistory) {
                result.getOrNull()?.let { genResult ->
                    // Will be saved after completion
                }
            }
        }
    }

    private fun generateTaskId(): String {
        return "task_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Use case for polling task status until completion
 */
class PollTaskStatusUseCase @Inject constructor(
    private val generationRepository: GenerationRepository
) {
    suspend operator fun invoke(taskId: String): Result<GenerationResult> {
        return generationRepository.waitForCompletion(taskId)
    }
}

/**
 * Use case for saving generation results to history
 */
class SaveToHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(
        taskId: String,
        type: GenerationType,
        prompt: String,
        resultUrl: String?,
        modelName: String
    ): Long {
        val item = HistoryItem(
            taskId = taskId,
            type = type,
            prompt = prompt,
            thumbnailUrl = resultUrl ?: "",
            resultUrl = resultUrl,
            modelName = modelName,
            createdAt = System.currentTimeMillis()
        )
        return historyRepository.saveHistoryItem(item)
    }
}

/**
 * Use case for getting generation history
 */
class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<HistoryItem>> {
        return historyRepository.getAllHistory()
    }

    fun byType(type: GenerationType): Flow<List<HistoryItem>> {
        return historyRepository.getHistoryByType(type)
    }
}

/**
 * Use case for getting available models
 */
class GetModelsUseCase @Inject constructor(
    private val generationRepository: GenerationRepository
) {
    suspend operator fun invoke(): Result<List<AiModel>> {
        return generationRepository.getModels()
    }
}

/**
 * Use case for validating API key
 */
class ValidateApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return settingsRepository.validateApiKey()
    }
}

/**
 * Use case for saving API key
 */
class SaveApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(apiKey: String): Result<Unit> {
        return try {
            // Validate before saving
            if (apiKey.isBlank()) {
                Result.failure(IllegalArgumentException("API key cannot be empty"))
            } else if (apiKey.length < 10) {
                Result.failure(IllegalArgumentException("API key is too short"))
            } else {
                settingsRepository.saveApiKey(apiKey)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for deleting history item
 */
class DeleteHistoryItemUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(id: Long) {
        historyRepository.deleteHistoryItem(id)
    }
}

/**
 * Use case for clearing all history
 */
class ClearHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() {
        historyRepository.clearAllHistory()
    }
}
