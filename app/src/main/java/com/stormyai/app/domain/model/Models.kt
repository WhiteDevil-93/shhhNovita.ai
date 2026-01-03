package com.stormyai.app.domain.model

enum class ModelType {
    IMAGE_GENERATION,
    IMAGE_TO_IMAGE,
    VIDEO_GENERATION
}

enum class GenerationType {
    TEXT_TO_IMAGE,
    IMAGE_TO_IMAGE,
    TEXT_TO_VIDEO
}

enum class TaskStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED
}

data class AiModel(
    val id: String,
    val name: String,
    val type: ModelType,
    val supportsNegativePrompt: Boolean,
    val supportsHighResolution: Boolean
)

data class GenerationTask(
    val prompt: String,
    val negativePrompt: String?,
    val width: Int,
    val height: Int,
    val steps: Int,
    val cfgScale: Float,
    val modelId: String?,
    val loras: List<Lora>,
    val type: GenerationType
)

data class Lora(
    val name: String,
    val weight: Float
)

data class GenerationResult(
    val taskId: String,
    val type: GenerationType,
    val status: TaskStatus,
    val imageUrl: String? = null,
    val videoUrl: String? = null
)

data class HistoryItem(
    val id: Long = 0,
    val taskId: String,
    val type: GenerationType,
    val prompt: String,
    val thumbnailUrl: String,
    val resultUrl: String,
    val modelName: String,
    val createdAt: Long
)

data class UserSettings(
    val apiKey: String? = null,
    val defaultModelId: String? = null,
    val defaultWidth: Int = 512,
    val defaultHeight: Int = 512,
    val defaultSteps: Int = 30,
    val defaultCfgScale: Float = 7.0f,
    val saveHistory: Boolean = true
)
