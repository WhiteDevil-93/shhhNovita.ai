package com.novitaai.studio.data.repository

import com.novitaai.studio.data.local.dao.HistoryDao
import com.novitaai.studio.data.local.entity.GenerationHistoryEntity
import com.novitaai.studio.domain.model.GenerationType
import com.novitaai.studio.domain.model.HistoryItem
import com.novitaai.studio.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of HistoryRepository using Room database
 */
@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<HistoryItem>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toHistoryItem() }
        }
    }

    override fun getHistoryByType(type: GenerationType): Flow<List<HistoryItem>> {
        return historyDao.getHistoryByType(type.name).map { entities ->
            entities.map { it.toHistoryItem() }
        }
    }

    override suspend fun saveHistoryItem(item: HistoryItem): Long {
        return historyDao.insertHistoryItem(item.toEntity())
    }

    override suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteHistoryItem(id)
    }

    override suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }

    override suspend fun getHistoryItemByTaskId(taskId: String): HistoryItem? {
        return historyDao.getHistoryItemByTaskId(taskId)?.toHistoryItem()
    }

    override suspend fun updateLocalPath(id: Long, localPath: String) {
        historyDao.updateLocalPath(id, localPath)
    }

    // Extension functions for mapping between domain and data models
    private fun GenerationHistoryEntity.toHistoryItem(): HistoryItem {
        return HistoryItem(
            id = id,
            taskId = taskId,
            type = GenerationType.valueOf(type),
            prompt = prompt,
            thumbnailUrl = thumbnailUrl,
            resultUrl = resultUrl,
            localPath = localPath,
            modelName = modelName,
            createdAt = createdAt
        )
    }

    private fun HistoryItem.toEntity(): GenerationHistoryEntity {
        return GenerationHistoryEntity(
            id = id,
            taskId = taskId,
            type = type.name,
            prompt = prompt,
            thumbnailUrl = thumbnailUrl,
            resultUrl = resultUrl,
            localPath = localPath,
            modelName = modelName,
            createdAt = createdAt
        )
    }
}
