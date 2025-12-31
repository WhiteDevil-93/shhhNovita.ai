package com.novitaai.studio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novitaai.studio.data.local.dao.HistoryDao
import com.novitaai.studio.data.local.entity.CachedModelEntity
import com.novitaai.studio.data.local.entity.GenerationHistoryEntity

/**
 * Room Database for NovitaAI Studio
 */
@Database(
    entities = [
        GenerationHistoryEntity::class,
        CachedModelEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        const val DATABASE_NAME = "novitaai_studio_db"
    }
}
