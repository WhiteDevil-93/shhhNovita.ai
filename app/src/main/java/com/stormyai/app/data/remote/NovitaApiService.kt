package com.stormyai.app.data.remote

import com.stormyai.app.data.remote.dto.TaskResponse
import com.stormyai.app.data.remote.dto.TaskStatusResponse
import com.stormyai.app.data.remote.dto.TextToImageRequest
import com.stormyai.app.data.remote.dto.TextToVideoRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NovitaApiService {
    @POST("/v3/async/txt2img")
    suspend fun textToImage(@Body request: TextToImageRequest): TaskResponse

    @POST("/v3/async/txt2video")
    suspend fun textToVideo(@Body request: TextToVideoRequest): TaskResponse

    @GET("/v3/async/task/{taskId}")
    suspend fun taskStatus(@Path("taskId") taskId: String): TaskStatusResponse
}
