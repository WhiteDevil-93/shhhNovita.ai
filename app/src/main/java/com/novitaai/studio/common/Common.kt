package com.novitaai.studio.common

/**
 * API Constants for Novita.ai
 */
object ApiConstants {
    const val BASE_URL = "https://api.novita.ai/"
    const val POLL_INTERVAL_MS = 2000L
    const val MAX_POLL_ATTEMPTS = 150
    const val DEFAULT_TIMEOUT_SECONDS = 30L
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 60L
    const val WRITE_TIMEOUT_SECONDS = 120L
}

/**
 * Common extensions and utilities
 */
object Extensions {
    /**
     * Format timestamp to readable date
     */
    fun Long.toFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(this))
    }

    /**
     * Truncate text to max length
     */
    fun String.truncate(maxLength: Int): String {
        return if (this.length > maxLength) {
            "${this.take(maxLength - 3)}..."
        } else {
            this
        }
    }

    /**
     * Convert bytes to human readable size
     */
    fun Long.toReadableSize(): String {
        if (this < 1024) return "$this B"
        val kb = this / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.1f GB", gb)
    }
}

/**
 * Resource wrapper for handling success/error states
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorMessageOrNull(): String? = (this as? Error)?.message

    inline fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> Loading
        }
    }

    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): Resource<T> {
        if (this is Error) action(message)
        return this
    }
}

/**
 * UI state wrapper for ViewModels
 */
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        fun <T> loading() = UiState<T>(isLoading = true)
        fun <T> success(data: T) = UiState(data = data)
        fun <T> error(message: String) = UiState<T>(error = message)
    }

    val hasData: Boolean get() = data != null
    val hasError: Boolean get() = error != null
}
