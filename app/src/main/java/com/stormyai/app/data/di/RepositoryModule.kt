package com.stormyai.app.data.di

import android.content.Context
import com.stormyai.app.data.local.dao.HistoryDao
import com.stormyai.app.data.remote.NovitaApiService
import com.stormyai.app.data.repository.GenerationRepositoryImpl
import com.stormyai.app.data.repository.HistoryRepositoryImpl
import com.stormyai.app.data.repository.SettingsRepositoryImpl
import com.stormyai.app.domain.repository.GenerationRepository
import com.stormyai.app.domain.repository.HistoryRepository
import com.stormyai.app.domain.repository.SettingsRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideGenerationRepository(api: NovitaApiService): GenerationRepository {
        return GenerationRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>,
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataStore, context)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(dao: HistoryDao): HistoryRepository {
        return HistoryRepositoryImpl(dao)
    }
}
