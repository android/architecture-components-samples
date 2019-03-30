/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.example.github

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher

import java.util.concurrent.Executor
import java.util.concurrent.Executors

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Global executor pools for the whole application.
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
@Singleton
open class AppExecutors(
    val io: CoroutineDispatcher,
    val default : CoroutineDispatcher,
    val mainThread: CoroutineDispatcher
) {
    @Inject
    constructor() : this(
        Dispatchers.IO,
        Dispatchers.Default,
        Dispatchers.Main
    )

    // temporary fields during migration to coroutines
    private val diskExecutor = DispatcherExecutor(io)
    private val networkExecutor = DispatcherExecutor(io)
    private val mainExecutor = DispatcherExecutor(mainThread)

    @Deprecated("use dispatchers")
    fun diskIO(): Executor {
        return diskExecutor
    }

    @Deprecated("use dispatchers")
    fun networkIO(): Executor {
        return networkExecutor
    }

    @Deprecated("use dispatchers")
    fun mainThread(): Executor {
        return mainExecutor
    }
}

/**
 * Temporary class during migration from executors to Coroutines
 */
private class DispatcherExecutor(val dispatcher : CoroutineDispatcher) : Executor {
    override fun execute(command: java.lang.Runnable) {
        dispatcher.dispatch(EmptyCoroutineContext, command)
    }
}