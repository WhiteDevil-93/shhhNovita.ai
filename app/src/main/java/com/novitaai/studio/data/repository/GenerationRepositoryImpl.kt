package com.novitaai.studio.data.repository

import android.util.Base64
import com.novitaai.studio.data.remote.NovitaApiService
import com.novitaai.studio.data.remote.dto.*
import com.novitaai.studio.domain.model.*
import com.novitaai.studio.domain.repository.GenerationRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GenerationRepository for Novita.ai API operations
 */
@Singleton
class GenerationRepositoryImpl @Inject constructor(
    private val apiService: NovitaApiService
) : GenerationRepository {

    override suspend fun generateImage(task: GenerationTask): Result<GenerationResult> {
        return try {
            val request = Txt2ImgRequest(
                modelName = task.modelName,
                prompt = task.prompt,
                negativePrompt = task.negativePrompt.ifBlank { null },
                width = task.width,
                height = task.height,
                steps = task.steps,
                cfgScale = task.cfgScale,
                seed = task.seed
            )

            val response = apiService.generateTextToImage(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val taskId = response.body()!!.data!!.taskId
                Result.success(
                    GenerationResult(
                        taskId = taskId,
                        type = GenerationType.TEXT_TO_IMAGE,
                        status = TaskStatus.PENDING
                    )
                )
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun transformImage(
        task: GenerationTask,
        sourceImageBytes: ByteArray
    ): Result<GenerationResult> {
        return try {
            val base64Image = Base64.encodeToString(sourceImageBytes, Base64.NO_WRAP)

            val request = Img2ImgRequest(
                modelName = task.modelName,
                prompt = task.prompt,
                negativePrompt = task.negativePrompt.ifBlank { null },
                images = listOf(base64Image),
                width = task.width,
                height = task.height,
                steps = task.steps,
                cfgScale = task.cfgScale,
                seed = task.seed
            )

            val response = apiService.generateImageToImage(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val taskId = response.body()!!.data!!.taskId
                Result.success(
                    GenerationResult(
                        taskId = taskId,
                        type = GenerationType.IMAGE_TO_IMAGE,
                        status = TaskStatus.PENDING
                    )
                )
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateVideo(task: GenerationTask): Result<GenerationResult> {
        return try {
            val request = Txt2VideoRequest(
                modelName = task.modelName,
                prompt = task.prompt,
                negativePrompt = task.negativePrompt.ifBlank { null },
                steps = task.steps,
                cfgScale = task.cfgScale,
                seed = task.seed
            )

            val response = apiService.generateTextToVideo(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val taskId = response.body()!!.data!!.taskId
                Result.success(
                    GenerationResult(
                        taskId = taskId,
                        type = GenerationType.TEXT_TO_VIDEO,
                        status = TaskStatus.PENDING
                    )
                )
            } else {
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun imageToVideo(
        task: GenerationTask,
        sourceImageBytes: ByteArray
    ): Result<GenerationResult> {
        // Implementation would require multipart upload
        // Simplified version here
        return Result.failure(Exception("Image-to-Video not yet implemented"))
    }

    override suspend fun pollTaskStatus(taskId: String): Result<GenerationResult> {
        return try {
            val response = apiService.getTaskStatus(taskId)

            if (response.isSuccessful && response.body()?.data != null) {
                val statusResponse = response.body()!!.data!!

                val status = when (statusResponse.status.uppercase()) {
                    "PENDING", "QUEUE" -> TaskStatus.PENDING
                    "PROCESSING", "RUNNING" -> TaskStatus.PROCESSING
                    "SUCCESS", "DONE" -> TaskStatus.SUCCESS
                    "FAILED", "ERROR" -> TaskStatus.FAILED
                    "CANCELLED", "CANCEL" -> TaskStatus.CANCELLED
                    else -> TaskStatus.PENDING
                }

                val imageUrl = statusResponse.result?.images?.firstOrNull()
                    ?: statusResponse.result?.image
                val videoUrl = statusResponse.result?.videos?.firstOrNull()
                    ?: statusResponse.result?.video

                val result = GenerationResult(
                    taskId = taskId,
                    type = when {
                        imageUrl != null -> GenerationType.TEXT_TO_IMAGE
                        videoUrl != null -> GenerationType.TEXT_TO_VIDEO
                        else -> GenerationType.TEXT_TO_IMAGE
                    },
                    status = status,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl,
                    errorMessage = statusResponse.error?.message
                )

                Result.success(result)
            } else {
                Result.failure(Exception("Failed to poll task status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun waitForCompletion(taskId: String, maxWaitMs: Long): Result<GenerationResult> {
        val startTime = System.currentTimeMillis()
        val pollIntervalMs = 2000L

        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            val result = pollTaskStatus(taskId)

            if (result.isFailure) {
                return result
            }

            val status = result.getOrNull()!!
            when (status.status) {
                TaskStatus.SUCCESS -> return result
                TaskStatus.FAILED, TaskStatus.CANCELLED -> return result
                TaskStatus.PENDING, TaskStatus.PROCESSING -> {
                    delay(pollIntervalMs)
                }
            }
        }

        return Result.failure(Exception("Timeout waiting for task completion"))
    }

    override suspend fun getModels(): Result<List<AiModel>> {
        return try {
            val response = apiService.getModels()

            if (response.isSuccessful && response.body()?.data != null) {
                val models = response.body()!!.data!!.map { dto ->
                    AiModel(
                        name = dto.name,
                        displayName = dto.displayName ?: dto.name,
                        type = when (dto.type?.lowercase()) {
                            "text-to-image", "txt2img" -> ModelType.IMAGE_GENERATION
                            "image-to-image", "img2img" -> ModelType.IMAGE_GENERATION
                            "text-to-video", "txt2video" -> ModelType.VIDEO_GENERATION
                            "upscale" -> ModelType.UPSCALING
                            "inpainting" -> ModelType.INPAINTING
                            else -> ModelType.IMAGE_GENERATION
                        },
                        isNSFW = dto.isNsfw ?: true,
                        isRecommended = dto.isRecommended ?: false
                    )
                }
                Result.success(models)
            } else {
                // Return default models if API fails
                Result.success(getDefaultModels())
            }
        } catch (e: Exception) {
            // Return default models on error
            Result.success(getDefaultModels())
        }
    }

    private fun getDefaultModels(): List<AiModel> {
        return listOf(
            AiModel("meinamix_v11", "MeinaMix V11", ModelType.IMAGE_GENERATION, true, true),
            AiModel("anylorav15", "AnyLora V1.5", ModelType.IMAGE_GENERATION, true, true),
            AiModel("revAnimated_v122", "Rev Animated V1.2.2", ModelType.IMAGE_GENERATION, false, true),
            AiModel("dreamshaper_8", "DreamShaper 8", ModelType.IMAGE_GENERATION, false, true),
            AiModel("realisticVision_v51", "Realistic Vision V5.1", ModelType.IMAGE_GENERATION, false, false),
            AiModel("stableDiffusionXL", "Stable Diffusion XL", ModelType.IMAGE_GENERATION, false, false),
            AiModel("stableVideoDiffusion", "Stable Video Diffusion", ModelType.VIDEO_GENERATION, false, false)
        )
    }
}
