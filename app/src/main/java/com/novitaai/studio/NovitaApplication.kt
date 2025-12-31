package com.novitaai.studio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for NovitaAI Studio
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class NovitaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}
