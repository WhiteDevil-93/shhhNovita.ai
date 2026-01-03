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
 * This interceptor observes settings changes and maintains an up-to-date API key
 * in memory to avoid blocking the network thread during authentication.
 * 
 * Note: The coroutine scope is intentionally application-scoped since this is a
 * singleton injected by Hilt and should live for the entire app lifecycle.
 */
class AuthInterceptor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val apiKeyRef = AtomicReference<String?>(null)

    init {
        // Initialize and observe API key changes
        // This Flow collection runs for the lifetime of the app, which is intentional
        // for a singleton component managed by Hilt
        scope.launch {
            settingsRepository.getSettings().collect { settings ->
                apiKeyRef.set(settings.apiKey)
            }
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
