package com.stormyai.app.data.repository

import com.stormyai.app.data.remote.NovitaApiService
import com.stormyai.app.data.remote.dto.TaskResponse
import com.stormyai.app.domain.model.AiModel
import com.stormyai.app.domain.model.GenerationResult
import com.stormyai.app.domain.model.GenerationTask
import com.stormyai.app.domain.model.GenerationType
import com.stormyai.app.domain.model.ModelType
import com.stormyai.app.domain.model.TaskStatus
import com.stormyai.app.domain.repository.GenerationRepository

class GenerationRepositoryImpl(
    private val api: NovitaApiService
) : GenerationRepository {

    override suspend fun generateImage(task: GenerationTask): Result<GenerationResult> {
        return runCatching {
            val response = api.textToImage(task.toImageRequest())
            response.toResult(GenerationType.TEXT_TO_IMAGE)
        }
    }

    override suspend fun generateVideo(task: GenerationTask): Result<GenerationResult> {
        return runCatching {
            val response = api.textToVideo(task.toVideoRequest())
            response.toResult(GenerationType.TEXT_TO_VIDEO)
        }
    }

    override suspend fun pollTaskStatus(taskId: String): Result<TaskStatus> {
        return runCatching {
            val response = api.taskStatus(taskId)
            response.status.toTaskStatus()
        }
    }

    /**
     * Returns a hardcoded list of available AI models.
     * 
     * Note: This is a temporary implementation returning static model profiles.
     * TODO: Replace with actual API call to fetch models from the backend.
     */
    override suspend fun getModels(): Result<List<AiModel>> {
        return Result.success(
            getDefaultProfiles().map { profile ->
                AiModel(
                    id = profile.modelId,
                    name = profile.name,
                    type = profile.type,
                    supportsNegativePrompt = true,
                    supportsHighResolution = true
                )
            }
        )
    }

    /**
     * Returns a hardcoded list of model profiles with pre-configured settings.
     * 
     * Note: This is a temporary implementation returning static profiles.
     * TODO: Replace with actual API call to fetch profiles from the backend.
     */
    override suspend fun getProfiles(): Result<List<com.stormyai.app.domain.model.ModelProfile>> {
        return Result.success(getDefaultProfiles())
    }

    private fun getDefaultProfiles(): List<com.stormyai.app.domain.model.ModelProfile> {
        return listOf(
            com.stormyai.app.domain.model.ModelProfile(
                id = "realism_base",
                name = "Realism Base (Model + VAE)",
                modelId = "meinamix_v11",
                vaeId = "vae-clear",
                type = ModelType.IMAGE_GENERATION,
                capabilities = setOf(
                    com.stormyai.app.domain.model.ModelCapability.REALISTIC,
                    com.stormyai.app.domain.model.ModelCapability.PORTRAIT
                ),
                nsfwAllowed = false
            ),
            com.stormyai.app.domain.model.ModelProfile(
                id = "cinematic_realism",
                name = "Cinematic Realism (Model + VAE)",
                modelId = "dreamshaper_v8",
                vaeId = "vae-clarity",
                type = ModelType.IMAGE_GENERATION,
                capabilities = setOf(
                    com.stormyai.app.domain.model.ModelCapability.REALISTIC
                ),
                nsfwAllowed = false
            ),
            com.stormyai.app.domain.model.ModelProfile(
                id = "nsfw_realism",
                name = "NSFW Realism (Model + VAE)",
                modelId = "realistic_vision_v6",
                vaeId = "vae-nsfw",
                type = ModelType.IMAGE_GENERATION,
                capabilities = setOf(
                    com.stormyai.app.domain.model.ModelCapability.REALISTIC,
                    com.stormyai.app.domain.model.ModelCapability.NSFW
                ),
                nsfwAllowed = true
            )
        )
    }

    private fun GenerationTask.toImageRequest() =
        com.stormyai.app.data.remote.dto.TextToImageRequest(
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            steps = steps,
            cfgScale = cfgScale,
            sampler = sampler,
            imageCount = imageCount,
            seed = seed,
            highResFix = highResFix,
            faceRestore = faceRestore,
            nsfw = nsfw,
            modelId = modelId,
            vaeId = vaeId,
            loras = loras.map { it.toRequest() }
        )

    private fun GenerationTask.toVideoRequest() =
        com.stormyai.app.data.remote.dto.TextToVideoRequest(
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            steps = steps,
            cfgScale = cfgScale,
            sampler = sampler,
            imageCount = imageCount,
            seed = seed,
            highResFix = highResFix,
            faceRestore = faceRestore,
            nsfw = nsfw,
            modelId = modelId,
            vaeId = vaeId,
            loras = loras.map { it.toRequest() }
        )

    private fun com.stormyai.app.domain.model.Lora.toRequest() =
        com.stormyai.app.data.remote.dto.LoraRequest(name = name, weight = weight)

    private fun TaskResponse.toResult(type: GenerationType) = GenerationResult(
        taskId = taskId,
        type = type,
        status = status.toTaskStatus(),
        imageUrl = imageUrl,
        videoUrl = videoUrl
    )

    private fun String.toTaskStatus(): TaskStatus = when (lowercase()) {
        "pending" -> TaskStatus.PENDING
        "running" -> TaskStatus.RUNNING
        "succeeded" -> TaskStatus.SUCCEEDED
        "failed" -> TaskStatus.FAILED
        else -> TaskStatus.PENDING
    }
}
