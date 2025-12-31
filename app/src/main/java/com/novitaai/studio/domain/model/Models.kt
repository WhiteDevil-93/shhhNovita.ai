package com.novitaai.studio.domain.model

/**
 * Represents a generation task submitted to Novita.ai API
 */
data class GenerationTask(
    val id: String,
    val type: GenerationType,
    val prompt: String,
    val negativePrompt: String = "",
    val modelName: String = "meinamix_v11",
    val width: Int = 512,
    val height: Int = 768,
    val steps: Int = 25,
    val cfgScale: Float = 7.0f,
    val seed: Int? = null,
    val sourceImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Types of media generation supported by the API
 */
enum class GenerationType {
    TEXT_TO_IMAGE,
    IMAGE_TO_IMAGE,
    TEXT_TO_VIDEO,
    IMAGE_TO_VIDEO,
    UPSCALE,
    INPAINTING
}

/**
 * Represents the result of a generation task
 */
data class GenerationResult(
    val taskId: String,
    val type: GenerationType,
    val status: TaskStatus,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val errorMessage: String? = null,
    val completedAt: Long? = null
)

/**
 * Status of a generation task
 */
enum class TaskStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED
}

/**
 * AI Model information
 */
data class AiModel(
    val name: String,
    val displayName: String,
    val type: ModelType,
    val isNSFW: Boolean = true,
    val isRecommended: Boolean = false
)

/**
 * Types of models available
 */
enum class ModelType {
    IMAGE_GENERATION,
    VIDEO_GENERATION,
    UPSCALING,
    INPAINTING,
    CONTROLNET
}

/**
 * User settings for the app
 */
data class UserSettings(
    val apiKey: String = "",
    val defaultModel: String = "meinamix_v11",
    val defaultWidth: Int = 512,
    val defaultHeight: Int = 768,
    val defaultSteps: Int = 25,
    val defaultCfgScale: Float = 7.0f,
    val saveHistory: Boolean = true,
    val autoDownload: Boolean = false,
    val darkMode: Boolean = true
)

/**
 * History item for saved generations
 */
data class HistoryItem(
    val id: Long = 0,
    val taskId: String,
    val type: GenerationType,
    val prompt: String,
    val thumbnailUrl: String,
    val resultUrl: String?,
    val localPath: String? = null,
    val modelName: String,
    val createdAt: Long
)
