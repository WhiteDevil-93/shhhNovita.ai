package com.stormyai.app.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.stormyai.app.common.NOVITA_BASE_URL
import com.stormyai.app.data.local.AppDatabase
import com.stormyai.app.data.remote.AuthInterceptor
import com.stormyai.app.data.remote.NovitaApiService
import com.stormyai.app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Modules {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("stormyai_settings") }
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "stormyai.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideHistoryDao(database: AppDatabase) = database.historyDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(settingsRepository: SettingsRepository): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(settingsRepository))
            .build()
    }

    @Provides
    @Singleton
    fun provideNovitaApi(client: OkHttpClient): NovitaApiService {
        return Retrofit.Builder()
            .baseUrl(NOVITA_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NovitaApiService::class.java)
    }
}
