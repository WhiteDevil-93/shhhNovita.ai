package com.stormyai.app.data.remote

import com.stormyai.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val settingsRepository: SettingsRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val apiKey = runBlocking {
            settingsRepository.getSettings().first().apiKey
        }
        val authenticated = if (apiKey.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
        }
        return chain.proceed(authenticated)
    }
}
