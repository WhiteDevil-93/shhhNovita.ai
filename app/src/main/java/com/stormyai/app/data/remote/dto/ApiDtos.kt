package com.stormyai.app.data.remote.dto

import com.google.gson.annotations.SerializedName


data class TextToImageRequest(
    @SerializedName("prompt") val prompt: String,
    @SerializedName("negative_prompt") val negativePrompt: String?,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("steps") val steps: Int,
    @SerializedName("cfg_scale") val cfgScale: Float,
    @SerializedName("model_id") val modelId: String?,
    @SerializedName("loras") val loras: List<LoraRequest>
)

data class TextToVideoRequest(
    @SerializedName("prompt") val prompt: String,
    @SerializedName("negative_prompt") val negativePrompt: String?,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("steps") val steps: Int,
    @SerializedName("cfg_scale") val cfgScale: Float,
    @SerializedName("model_id") val modelId: String?,
    @SerializedName("loras") val loras: List<LoraRequest>
)

data class LoraRequest(
    @SerializedName("name") val name: String,
    @SerializedName("weight") val weight: Float
)

data class TaskResponse(
    @SerializedName("task_id") val taskId: String,
    @SerializedName("status") val status: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("video_url") val videoUrl: String?
)

data class TaskStatusResponse(
    @SerializedName("task_id") val taskId: String,
    @SerializedName("status") val status: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("video_url") val videoUrl: String?
)
