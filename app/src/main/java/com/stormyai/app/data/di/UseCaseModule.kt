package com.stormyai.app.data.di

import com.stormyai.app.domain.repository.GenerationRepository
import com.stormyai.app.domain.repository.HistoryRepository
import com.stormyai.app.domain.repository.SettingsRepository
import com.stormyai.app.domain.usecase.CreateImageUseCase
import com.stormyai.app.domain.usecase.GetHistoryUseCase
import com.stormyai.app.domain.usecase.GetModelsUseCase
import com.stormyai.app.domain.usecase.GetProfilesUseCase
import com.stormyai.app.domain.usecase.PollTaskStatusUseCase
import com.stormyai.app.domain.usecase.SaveToHistoryUseCase
import com.stormyai.app.domain.usecase.UpdateSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideCreateImageUseCase(
        generationRepository: GenerationRepository,
        settingsRepository: SettingsRepository,
        historyRepository: HistoryRepository
    ): CreateImageUseCase {
        return CreateImageUseCase(generationRepository, settingsRepository, historyRepository)
    }

    @Provides
    @Singleton
    fun provideGetHistoryUseCase(historyRepository: HistoryRepository): GetHistoryUseCase {
        return GetHistoryUseCase(historyRepository)
    }

    @Provides
    @Singleton
    fun provideSaveToHistoryUseCase(historyRepository: HistoryRepository): SaveToHistoryUseCase {
        return SaveToHistoryUseCase(historyRepository)
    }

    @Provides
    @Singleton
    fun providePollTaskStatusUseCase(generationRepository: GenerationRepository): PollTaskStatusUseCase {
        return PollTaskStatusUseCase(generationRepository)
    }

    @Provides
    @Singleton
    fun provideGetModelsUseCase(generationRepository: GenerationRepository): GetModelsUseCase {
        return GetModelsUseCase(generationRepository)
    }

    @Provides
    @Singleton
    fun provideGetProfilesUseCase(generationRepository: GenerationRepository): GetProfilesUseCase {
        return GetProfilesUseCase(generationRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateSettingsUseCase(settingsRepository: SettingsRepository): UpdateSettingsUseCase {
        return UpdateSettingsUseCase(settingsRepository)
    }
}
