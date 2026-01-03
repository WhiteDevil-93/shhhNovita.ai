package com.stormyai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.stormyai.app.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(item: HistoryEntity): Long

    @Query("SELECT * FROM history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<HistoryEntity>>
}
