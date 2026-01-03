package com.stormyai.app.domain.repository

import com.stormyai.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface GenerationRepository {
    suspend fun generateImage(task: GenerationTask): Result<GenerationResult>
    suspend fun generateVideo(task: GenerationTask): Result<GenerationResult>
    suspend fun pollTaskStatus(taskId: String): Result<TaskStatus>
    fun getModels(): Result<List<AiModel>>
}

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun updateSettings(settings: UserSettings)
}

interface HistoryRepository {
    suspend fun saveHistoryItem(item: HistoryItem): Long
    fun getAllHistory(): Flow<List<HistoryItem>>
}
