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

package com.example.background.multiprocess;

import android.content.Context;
import android.util.Log;

import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteListenableWorker;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Example of implementing a RemoteListenableWorker. This worker simply returns Success.
 * <p>
 * Use RemoteListenableWorker if your worker is implemented in Java, otherwise use
 * RemoteCoroutineWorker if your worker is implemented in Kotlin.
 */
public class ExampleRemoteListenableWorker extends RemoteListenableWorker {

    private static final String TAG = "ListenableWorker";

    public ExampleRemoteListenableWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Log.i(TAG, "Starting ExampleRemoteListenableWorker");

            // Do some work here.

            return completer.set(Result.success());
        });
    }
}
