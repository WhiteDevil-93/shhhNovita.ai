package com.stormyai.app.domain.model

enum class ModelType {
    IMAGE_GENERATION,
    IMAGE_TO_IMAGE,
    VIDEO_GENERATION
}

enum class ModelCapability {
    REALISTIC,
    ANIME,
    NSFW,
    PORTRAIT
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

data class ModelProfile(
    val id: String,
    val name: String,
    val modelId: String,
    val vaeId: String?,
    val type: ModelType,
    val capabilities: Set<ModelCapability>,
    val nsfwAllowed: Boolean
)

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
    val sampler: String,
    val imageCount: Int,
    val seed: Long?,
    val highResFix: Boolean,
    val faceRestore: Boolean,
    val nsfw: Boolean,
    val modelId: String?,
    val vaeId: String?,
    val profileId: String?,
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
    val negativePrompt: String?,
    val thumbnailUrl: String,
    val resultUrl: String,
    val modelName: String,
    val sampler: String,
    val steps: Int,
    val cfgScale: Float,
    val imageCount: Int,
    val seed: Long?,
    val highResFix: Boolean,
    val faceRestore: Boolean,
    val nsfw: Boolean,
    val createdAt: Long
)

data class UserSettings(
    val apiKey: String? = null,
    val defaultModelId: String? = null,
    val defaultSampler: String = "DPM++ SDE Karras",
    val defaultWidth: Int = 512,
    val defaultHeight: Int = 512,
    val defaultSteps: Int = 30,
    val defaultCfgScale: Float = 7.0f,
    val saveHistory: Boolean = true
)
