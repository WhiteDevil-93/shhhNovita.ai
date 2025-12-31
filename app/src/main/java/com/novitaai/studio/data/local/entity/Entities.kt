package com.novitaai.studio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing generation history
 */
@Entity(tableName = "generation_history")
data class GenerationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: String,
    val type: String, // GenerationType as string
    val prompt: String,
    val thumbnailUrl: String,
    val resultUrl: String?,
    val localPath: String?,
    val modelName: String,
    val createdAt: Long
)

/**
 * Room entity for caching model information
 */
@Entity(tableName = "cached_models")
data class CachedModelEntity(
    @PrimaryKey
    val name: String,
    val displayName: String,
    val type: String,
    val isNsfw: Boolean,
    val isRecommended: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)
