/*
 * Copyright 2021 The Android Open Source Project
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

package com.example.background.multiprocess

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.multiprocess.RemoteCoroutineWorker
import androidx.work.multiprocess.RemoteListenableWorker

/**
 * Example of implementing a RemoteCoroutineWorker. This worker simply returns Success.
 *
 * Use RemoteCoroutineWorker if your worker is implemented in Kotlin, otherwise use
 * [RemoteListenableWorker] if your worker is implemented in Java.
 */
class ExampleRemoteCoroutineWorker(context: Context, parameters: WorkerParameters) :
    RemoteCoroutineWorker(context, parameters) {

    override suspend fun doRemoteWork(): Result {

        Log.d(TAG, "Starting ExampleRemoteCoroutineWorker")

        // Do some work here

        return Result.success()
    }

    companion object {
        private const val TAG = "CoroutineWorker"
    }
}