package com.stormyai.app.data.remote

import com.stormyai.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference

/**
 * OkHttp interceptor that adds authentication headers to API requests.
 * 
 * PERFORMANCE NOTE: This implementation uses runBlocking() to retrieve the API key
 * from DataStore, which blocks the network thread. This is a known limitation that
 * can potentially cause thread starvation or ANR issues in high-traffic scenarios.
 * 
 * TODO: Consider restructuring the authentication flow to avoid blocking operations
 * in the interceptor, such as:
 * - Passing the API key synchronously through a different mechanism
 * - Caching the API key in memory with reactive updates
 * - Using a custom authenticator pattern
 */
class AuthInterceptor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val apiKeyRef = AtomicReference<String?>(null)

    init {
        scope.launch {
            val settings = settingsRepository.getSettings().first()
            apiKeyRef.set(settings.apiKey)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val apiKey = apiKeyRef.get()
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
