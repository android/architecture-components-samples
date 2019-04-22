package com.example.background

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * The [Application]. Responsible for initializing [WorkManager] in [Log.VERBOSE] mode.
 */
class App : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration() =
            Configuration.Builder()
                    .setMinimumLoggingLevel(Log.VERBOSE)
                    .build()
}

