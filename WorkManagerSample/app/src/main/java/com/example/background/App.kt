package com.example.background

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * The [Application]. Responsible for initializing [WorkManager] in [Log.VERBOSE] mode.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = Configuration.Builder()
                .setMinimumLoggingLevel(Log.VERBOSE)
                .build()

        WorkManager.initialize(this, configuration)
    }
}
