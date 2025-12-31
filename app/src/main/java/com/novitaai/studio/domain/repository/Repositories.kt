package com.novitaai.studio.domain.repository

import com.novitaai.studio.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for generation operations
 * Defines the contract for image/video generation functionality
 */
interface GenerationRepository {
    /**
     * Submit a text-to-image generation request
     */
    suspend fun generateImage(task: GenerationTask): Result<GenerationResult>

    /**
     * Submit an image-to-image generation request
     */
    suspend fun transformImage(task: GenerationTask, sourceImageBytes: ByteArray): Result<GenerationResult>

    /**
     * Submit a text-to-video generation request
     */
    suspend fun generateVideo(task: GenerationTask): Result<GenerationResult>

    /**
     * Submit an image-to-video generation request
     */
    suspend fun imageToVideo(task: GenerationTask, sourceImageBytes: ByteArray): Result<GenerationResult>

    /**
     * Poll the status of a generation task
     */
    suspend fun pollTaskStatus(taskId: String): Result<GenerationResult>

    /**
     * Wait for a task to complete with periodic polling
     */
    suspend fun waitForCompletion(taskId: String, maxWaitMs: Long = 300000): Result<GenerationResult>

    /**
     * Get list of available models
     */
    suspend fun getModels(): Result<List<AiModel>>
}

/**
 * Repository interface for user settings
 */
interface SettingsRepository {
    /**
     * Get the current API key
     */
    fun getApiKey(): Flow<String>

    /**
     * Save the API key
     */
    suspend fun saveApiKey(apiKey: String)

    /**
     * Clear the API key
     */
    suspend fun clearApiKey()

    /**
     * Get user settings as a flow
     */
    fun getSettings(): Flow<UserSettings>

    /**
     * Save user settings
     */
    suspend fun saveSettings(settings: UserSettings)

    /**
     * Check if API key is set and valid
     */
    suspend fun validateApiKey(): Result<Boolean>
}

/**
 * Repository interface for generation history
 */
interface HistoryRepository {
    /**
     * Get all history items as a flow
     */
    fun getAllHistory(): Flow<List<HistoryItem>>

    /**
     * Get history items by type
     */
    fun getHistoryByType(type: GenerationType): Flow<List<HistoryItem>>

    /**
     * Save a history item
     */
    suspend fun saveHistoryItem(item: HistoryItem): Long

    /**
     * Delete a history item
     */
    suspend fun deleteHistoryItem(id: Long)

    /**
     * Clear all history
     */
    suspend fun clearAllHistory()

    /**
     * Get a single history item by task ID
     */
    suspend fun getHistoryItemByTaskId(taskId: String): HistoryItem?

    /**
     * Update local path for a history item
     */
    suspend fun updateLocalPath(id: Long, localPath: String)
}
