package com.novitaai.studio.data.di

import com.novitaai.studio.data.repository.GenerationRepositoryImpl
import com.novitaai.studio.data.repository.HistoryRepositoryImpl
import com.novitaai.studio.data.repository.SettingsRepositoryImpl
import com.novitaai.studio.domain.repository.GenerationRepository
import com.novitaai.studio.domain.repository.HistoryRepository
import com.novitaai.studio.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGenerationRepository(
        generationRepositoryImpl: GenerationRepositoryImpl
    ): GenerationRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository
}
