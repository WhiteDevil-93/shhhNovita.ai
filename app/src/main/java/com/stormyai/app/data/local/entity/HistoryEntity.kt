package com.stormyai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val type: String,
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
