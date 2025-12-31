package com.novitaai.studio.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor to add API key to all requests
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val apiKey = apiKeyProvider.getApiKey()

        if (apiKey.isBlank()) {
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .build()

        return chain.proceed(newRequest)
    }
}

/**
 * Provider interface for API key
 */
interface ApiKeyProvider {
    fun getApiKey(): String
}

/**
 * Implementation that gets API key from DataStore
 */
@Singleton
class DataStoreApiKeyProvider @Inject constructor(
    private val dataStore: kotlinx.coroutines.flow.first
) : ApiKeyProvider {

    // This will be injected with actual DataStore access
    // Placeholder for compilation
    private var cachedKey: String = ""

    override fun getApiKey(): String {
        return cachedKey
    }

    fun setApiKey(key: String) {
        cachedKey = key
    }
}
