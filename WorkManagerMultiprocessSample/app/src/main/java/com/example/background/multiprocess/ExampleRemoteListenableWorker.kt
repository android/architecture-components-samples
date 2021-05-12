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
import androidx.work.multiprocess.RemoteListenableWorker
import com.google.common.util.concurrent.ListenableFuture
import androidx.concurrent.futures.CallbackToFutureAdapter
import kotlinx.coroutines.*

class ExampleRemoteListenableWorker(context: Context, parameters: WorkerParameters) :
    RemoteListenableWorker(context, parameters) {

    private var job: Job? = null

    override fun startRemoteWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            Log.d(TAG, "Starting ExampleRemoteListenableWorker")
            val scope = CoroutineScope(Dispatchers.Default)

            job = scope.launch {
                delay(5 * 1000)
            }

            job?.invokeOnCompletion {
                Log.d(TAG, "Completed ExampleRemoteListenableWorker")
                completer.set(Result.success())
            }
        }
    }

    override fun onStopped() {
        job?.cancel()
    }

    companion object {
        private const val TAG = "ListenableWorker"
    }
}