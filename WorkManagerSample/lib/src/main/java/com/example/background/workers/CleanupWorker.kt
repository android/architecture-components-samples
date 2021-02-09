/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants
import java.io.File

/** Clears temporary files. */
class CleanupWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            cleanupDirectory()
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Error cleaning up", exception)
            Result.failure()
        }
    }

    /** Removes pngs from the app's files directory */
    private fun cleanupDirectory() {
        File(applicationContext.filesDir, Constants.OUTPUT_PATH).apply {
            if (exists()) {
                listFiles()?.forEach { file ->
                    if (file.name.endsWith(".png")) {
                        val deleted = file.delete()
                        Log.i(TAG, "Deleted ${file.name} - $deleted")
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "CleanupWorker"
    }
}
