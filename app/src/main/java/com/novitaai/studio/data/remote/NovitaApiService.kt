package com.novitaai.studio.data.remote

import com.novitaai.studio.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Novita.ai API Service Interface
 * Defines all API endpoints for generation operations
 */
interface NovitaApiService {

    /**
     * Get user information (for API key validation)
     */
    @GET("v4/user")
    suspend fun getUserInfo(): Response<NovitaResponse<UserInfoResponse>>

    /**
     * Get list of available models
     */
    @GET("v4/models")
    suspend fun getModels(): Response<NovitaResponse<List<ModelInfo>>>

    // ============ ASYNC GENERATION ENDPOINTS ============

    /**
     * Submit text-to-image generation request
     */
    @POST("v3/async/txt2img")
    suspend fun generateTextToImage(
        @Body request: Txt2ImgRequest
    ): Response<NovitaResponse<TaskIdResponse>>

    /**
     * Submit image-to-image generation request
     */
    @POST("v3/async/img2img")
    suspend fun generateImageToImage(
        @Body request: Img2ImgRequest
    ): Response<NovitaResponse<TaskIdResponse>>

    /**
     * Submit text-to-video generation request
     */
    @POST("v3/async/txt2video")
    suspend fun generateTextToVideo(
        @Body request: Txt2VideoRequest
    ): Response<NovitaResponse<TaskIdResponse>>

    /**
     * Submit image-to-video generation request
     */
    @POST("v3/async/img2video")
    suspend fun generateImageToVideo(
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: okhttp3.RequestBody
    ): Response<NovitaResponse<TaskIdResponse>>

    /**
     * Poll task status
     */
    @GET("v3/async/task/{taskId}")
    suspend fun getTaskStatus(
        @Path("taskId") taskId: String
    ): Response<NovitaResponse<TaskStatusResponse>>

    // ============ ADDITIONAL GENERATION ENDPOINTS ============

    /**
     * Upscale image
     */
    @POST("v3/async/upscale")
    suspend fun upscale(
        @Body request: UpscaleRequest
    ): Response<NovitaResponse<TaskIdResponse>>

    /**
     * Inpainting
     */
    @POST("v3/async/inpainting")
    suspend fun inpainting(
        @Body request: InpaintingRequest
    ): Response<NovitaResponse<TaskIdResponse>>

    /**
     * ControlNet
     */
    @POST("v3/async/controlnet")
    suspend fun controlnet(
        @Body request: ControlNetRequest
    ): Response<NovitaResponse<TaskIdResponse>>
}

/**
 * Upscale request body
 */
data class UpscaleRequest(
    val image: String, // Base64 encoded
    val modelName: String = "RealESRGAN_x4plus",
    val scale: Int = 4
)

/**
 * Inpainting request body
 */
data class InpaintingRequest(
    val modelName: String,
    val prompt: String,
    val negativePrompt: String?,
    val image: String, // Base64 encoded
    val maskImage: String, // Base64 encoded
    val width: Int?,
    val height: Int?,
    val steps: Int?,
    val cfgScale: Float?,
    val maskBlur: Int? = 4,
    val maskMode: String? = "whole_mask",
    val inpaintFullRes: Boolean? = true
)

/**
 * ControlNet request body
 */
data class ControlNetRequest(
    val modelName: String,
    val prompt: String,
    val negativePrompt: String?,
    val controlnetImage: String, // Base64 encoded
    val controlType: String?,
    val width: Int?,
    val height: Int?,
    val steps: Int?,
    val cfgScale: Float?,
    val controlNetWeight: Float? = 1.0,
    val controlNetStartingControlStep: Int? = 0,
    val controlNetEndingControlStep: Int? = 1
)
