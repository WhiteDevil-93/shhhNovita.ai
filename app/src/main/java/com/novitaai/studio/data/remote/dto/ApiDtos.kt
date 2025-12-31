package com.novitaai.studio.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper from Novita.ai API
 */
data class NovitaResponse<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("request_id")
    val requestId: String?
)

/**
 * Task ID response from async operations
 */
data class TaskIdResponse(
    @SerializedName("task_id")
    val taskId: String
)

/**
 * Task status response
 */
data class TaskStatusResponse(
    @SerializedName("task_id")
    val taskId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("progress")
    val progress: Float?,
    @SerializedName("result")
    val result: TaskResult?,
    @SerializedName("error")
    val error: TaskError?
)

/**
 * Task result containing generated media
 */
data class TaskResult(
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("videos")
    val videos: List<String>?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("video")
    val video: String?
)

/**
 * Task error details
 */
data class TaskError(
    @SerializedName("message")
    val message: String?,
    @SerializedName("code")
    val code: String?
)

/**
 * Text-to-image request body
 */
data class Txt2ImgRequest(
    @SerializedName("model_name")
    val modelName: String,
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("negative_prompt")
    val negativePrompt: String?,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("image_num")
    val imageNum: Int = 1,
    @SerializedName("steps")
    val steps: Int?,
    @SerializedName("cfg_scale")
    val cfgScale: Float?,
    @SerializedName("seed")
    val seed: Int?,
    @SerializedName("clip_skip")
    val clipSkip: Int? = 1,
    @SerializedName("sampler_name")
    val samplerName: String? = "Euler a"
)

/**
 * Image-to-image request body
 */
data class Img2ImgRequest(
    @SerializedName("model_name")
    val modelName: String,
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("negative_prompt")
    val negativePrompt: String?,
    @SerializedName("images")
    val images: List<String>, // Base64 encoded images
    @SerializedName("width")
    val width: Int?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("steps")
    val steps: Int?,
    @SerializedName("cfg_scale")
    val cfgScale: Float?,
    @SerializedName("denoising_strength")
    val denoisingStrength: Float? = 0.75,
    @SerializedName("seed")
    val seed: Int?
)

/**
 * Text-to-video request body
 */
data class Txt2VideoRequest(
    @SerializedName("model_name")
    val modelName: String,
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("negative_prompt")
    val negativePrompt: String?,
    @SerializedName("video_length")
    val videoLength: Int = 25,
    @SerializedName("fps")
    val fps: Int = 8,
    @SerializedName("width")
    val width: Int = 512,
    @SerializedName("height")
    val height: Int = 576,
    @SerializedName("steps")
    val steps: Int?,
    @SerializedName("cfg_scale")
    val cfgScale: Float?,
    @SerializedName("seed")
    val seed: Int?
)

/**
 * User info response
 */
data class UserInfoResponse(
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("credits")
    val credits: Float?,
    @SerializedName("is_active")
    val isActive: Boolean?
)

/**
 * Model info response
 */
data class ModelInfo(
    @SerializedName("name")
    val name: String,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("is_nsfw")
    val isNsfw: Boolean?,
    @SerializedName("is_recommended")
    val isRecommended: Boolean?
)
