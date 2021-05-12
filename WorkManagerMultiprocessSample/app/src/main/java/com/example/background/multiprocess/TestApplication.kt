package com.example.background.multiprocess

import android.app.Application
import androidx.work.Configuration

class TestApplication : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setDefaultProcessName("com.example.background.multiprocess")
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}