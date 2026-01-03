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
    val modelId: String?,
    val type: GenerationType
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
    val defaultModelId: String? = null,
    val defaultWidth: Int = 512,
    val defaultHeight: Int = 512,
    val saveHistory: Boolean = true
)
