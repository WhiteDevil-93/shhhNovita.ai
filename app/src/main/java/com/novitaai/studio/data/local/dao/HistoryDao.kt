package com.novitaai.studio.data.local.dao

import androidx.room.*
import com.novitaai.studio.data.local.entity.GenerationHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for generation history operations
 */
@Dao
interface HistoryDao {

    /**
     * Get all history items ordered by creation date (newest first)
     */
    @Query("SELECT * FROM generation_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<GenerationHistoryEntity>>

    /**
     * Get history items by type
     */
    @Query("SELECT * FROM generation_history WHERE type = :type ORDER BY createdAt DESC")
    fun getHistoryByType(type: String): Flow<List<GenerationHistoryEntity>>

    /**
     * Get a single history item by ID
     */
    @Query("SELECT * FROM generation_history WHERE id = :id")
    suspend fun getHistoryItemById(id: Long): GenerationHistoryEntity?

    /**
     * Get a history item by task ID
     */
    @Query("SELECT * FROM generation_history WHERE taskId = :taskId")
    suspend fun getHistoryItemByTaskId(taskId: String): GenerationHistoryEntity?

    /**
     * Insert a new history item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: GenerationHistoryEntity): Long

    /**
     * Update local path for a history item
     */
    @Query("UPDATE generation_history SET localPath = :localPath WHERE id = :id")
    suspend fun updateLocalPath(id: Long, localPath: String)

    /**
     * Delete a history item by ID
     */
    @Query("DELETE FROM generation_history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Long)

    /**
     * Delete a history item by task ID
     */
    @Query("DELETE FROM generation_history WHERE taskId = :taskId")
    suspend fun deleteHistoryItemByTaskId(taskId: String)

    /**
     * Clear all history
     */
    @Query("DELETE FROM generation_history")
    suspend fun clearAllHistory()

    /**
     * Get history count
     */
    @Query("SELECT COUNT(*) FROM generation_history")
    suspend fun getHistoryCount(): Int

    /**
     * Search history by prompt
     */
    @Query("SELECT * FROM generation_history WHERE prompt LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchHistory(query: String): Flow<List<GenerationHistoryEntity>>
}
