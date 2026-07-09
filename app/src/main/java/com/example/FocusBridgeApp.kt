package com.example

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Focus Bridge main Application class.
 * Initializes Hilt dependency injection and integrates Hilt-powered WorkManager.
 */
@HiltAndroidApp
class FocusBridgeApp : Application(), Configuration.Provider {

    // Inject custom HiltWorkerFactory for Hilt-injected WorkManager Workers
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
