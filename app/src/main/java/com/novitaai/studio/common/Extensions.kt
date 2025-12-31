package com.novitaai.studio.common

/**
 * Extension function to format file size
 */
fun Long.toReadableFileSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", this / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

/**
 * Extension function to validate API key format
 */
fun String.isValidApiKey(): Boolean {
    return this.isNotBlank() && this.length >= 10
}

/**
 * Extension function to capitalize first letter
 */
fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        this[0].uppercaseChar() + substring(1)
    } else {
        this
    }
}
