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

    override fun getModels(): Result<List<AiModel>> {
        return Result.success(
            listOf(
                AiModel(
                    id = "meinamix_v11",
                    name = "MeinaMix v11",
                    type = ModelType.IMAGE_GENERATION,
                    supportsNegativePrompt = true,
                    supportsHighResolution = true
                ),
                AiModel(
                    id = "dreamshaper_v8",
                    name = "DreamShaper v8",
                    type = ModelType.IMAGE_GENERATION,
                    supportsNegativePrompt = true,
                    supportsHighResolution = true
                )
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
            modelId = modelId,
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
            modelId = modelId,
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
