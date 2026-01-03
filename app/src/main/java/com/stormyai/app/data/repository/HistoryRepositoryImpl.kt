package com.stormyai.app.data.repository

import com.stormyai.app.data.local.dao.HistoryDao
import com.stormyai.app.data.local.entity.HistoryEntity
import com.stormyai.app.domain.model.GenerationType
import com.stormyai.app.domain.model.HistoryItem
import com.stormyai.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val dao: HistoryDao
) : HistoryRepository {
    override suspend fun saveHistoryItem(item: HistoryItem): Long {
        return dao.insert(item.toEntity())
    }

    override fun getAllHistory(): Flow<List<HistoryItem>> {
        return dao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    private fun HistoryItem.toEntity() = HistoryEntity(
        id = id,
        taskId = taskId,
        type = type.name,
        prompt = prompt,
        thumbnailUrl = thumbnailUrl,
        resultUrl = resultUrl,
        modelName = modelName,
        createdAt = createdAt
    )

    private fun HistoryEntity.toDomain() = HistoryItem(
        id = id,
        taskId = taskId,
        type = GenerationType.valueOf(type),
        prompt = prompt,
        thumbnailUrl = thumbnailUrl,
        resultUrl = resultUrl,
        modelName = modelName,
        createdAt = createdAt
    )
}
