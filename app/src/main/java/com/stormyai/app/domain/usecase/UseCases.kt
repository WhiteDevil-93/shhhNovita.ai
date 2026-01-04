package com.stormyai.app.domain.usecase

import com.stormyai.app.domain.model.*
import com.stormyai.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Creates an image generation task.
 *
 * @return Result<GenerationResult> Success with generation result, or Failure with:
 *   - IllegalStateException if NSFW mode is requested with a profile that doesn't allow it
 *   - IOException for network errors
 *   - HttpException for API errors
 */
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
        sampler: String? = null,
        imageCount: Int? = null,
        seed: Long? = null,
        highResFix: Boolean = false,
        faceRestore: Boolean = false,
        nsfw: Boolean = false,
        profile: ModelProfile? = null,
        modelId: String? = null,
        loras: List<Lora> = emptyList()
    ): Result<GenerationResult> {
        val settings = settingsRepository.getSettings().first()
        val resolvedModelId = profile?.modelId ?: modelId ?: settings.defaultModelId
        if (nsfw && profile?.nsfwAllowed == false) {
            return Result.failure(IllegalStateException("Selected profile does not allow NSFW mode"))
        }
        val task = GenerationTask(
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width ?: settings.defaultWidth,
            height = height ?: settings.defaultHeight,
            steps = steps ?: settings.defaultSteps,
            cfgScale = cfgScale ?: settings.defaultCfgScale,
            sampler = sampler ?: settings.defaultSampler,
            imageCount = imageCount ?: 1,
            seed = seed,
            highResFix = highResFix,
            faceRestore = faceRestore,
            nsfw = nsfw,
            modelId = resolvedModelId,
            vaeId = profile?.vaeId,
            profileId = profile?.id,
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
                    negativePrompt = negativePrompt,
                    thumbnailUrl = generated.imageUrl.orEmpty(),
                    resultUrl = generated.imageUrl.orEmpty(),
                    modelName = profile?.name ?: resolvedModelId.orEmpty(),
                    sampler = task.sampler,
                    steps = task.steps,
                    cfgScale = task.cfgScale,
                    imageCount = task.imageCount,
                    seed = task.seed,
                    highResFix = task.highResFix,
                    faceRestore = task.faceRestore,
                    nsfw = task.nsfw,
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
        negativePrompt: String?,
        resultUrl: String,
        modelName: String,
        sampler: String,
        steps: Int,
        cfgScale: Float,
        imageCount: Int,
        seed: Long?,
        highResFix: Boolean,
        faceRestore: Boolean,
        nsfw: Boolean
    ): Long {
        val item = HistoryItem(
            taskId = taskId,
            type = type,
            prompt = prompt,
            negativePrompt = negativePrompt,
            thumbnailUrl = resultUrl,
            resultUrl = resultUrl,
            modelName = modelName,
            sampler = sampler,
            steps = steps,
            cfgScale = cfgScale,
            imageCount = imageCount,
            seed = seed,
            highResFix = highResFix,
            faceRestore = faceRestore,
            nsfw = nsfw,
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

/**
 * Polls the status of a generation task.
 *
 * @param taskId The unique identifier of the task to poll
 * @return Result<TaskStatus> Success with task status, or Failure with:
 *   - IOException for network errors
 *   - HttpException for API errors
 */
class PollTaskStatusUseCase(
    private val generationRepository: GenerationRepository
) {
    suspend operator fun invoke(taskId: String): Result<TaskStatus> {
        return generationRepository.pollTaskStatus(taskId)
    }
}

/**
 * Retrieves the list of available AI models.
 *
 * @return Result<List<AiModel>> Success with model list, or Failure with:
 *   - IOException for network errors
 *   - HttpException for API errors
 */
class GetModelsUseCase(
    private val generationRepository: GenerationRepository
) {
    suspend operator fun invoke(): Result<List<AiModel>> {
        return generationRepository.getModels()
    }
}

/**
 * Retrieves the list of available model profiles.
 *
 * @return Result<List<ModelProfile>> Success with profile list, or Failure with:
 *   - IOException for network errors
 *   - HttpException for API errors
 */
class GetProfilesUseCase(
    private val generationRepository: GenerationRepository
) {
    suspend operator fun invoke(): Result<List<ModelProfile>> {
        return generationRepository.getProfiles()
    }
}

class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: UserSettings) {
        settingsRepository.updateSettings(settings)
    }
}
