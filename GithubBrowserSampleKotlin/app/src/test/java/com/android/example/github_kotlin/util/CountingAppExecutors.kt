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

package com.android.example.github_kotlin.util

import com.android.example.github_kotlin.AppExecutors
import java.util.concurrent.*

class CountingAppExecutors {

    private val LOCK = Any()

    private var taskCount = 0

    val appExecutors: AppExecutors

    init {
        val increment = Runnable {
            synchronized(LOCK) {
                taskCount--
                if (taskCount == 0) {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (LOCK as java.lang.Object).notifyAll()
                }
            }
        }
        val decrement = Runnable {
            synchronized(LOCK) {
                taskCount++
            }
        }
        appExecutors = AppExecutors(
                CountingExecutor(increment, decrement),
                CountingExecutor(increment, decrement),
                CountingExecutor(increment, decrement))
    }

    @Throws(InterruptedException::class, TimeoutException::class)
    fun drainTasks(time: Int, timeUnit: TimeUnit) {
        val end = System.currentTimeMillis() + timeUnit.toMillis(time.toLong())
        while (true) {
            synchronized(LOCK) {
                if (taskCount == 0) {
                    return
                }
                val now = System.currentTimeMillis()
                val remaining = end - now
                if (remaining > 0) {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (LOCK as java.lang.Object).wait(remaining)
                } else {
                    throw TimeoutException("could not drain tasks")
                }
            }
        }
    }

    private class CountingExecutor(private val increment: Runnable, private val decrement: Runnable) : Executor {

        private val delegate = Executors.newSingleThreadExecutor()

        override fun execute(command: Runnable) {
            increment.run()
            delegate.execute {
                try {
                    command.run()
                } finally {
                    decrement.run()
                }
            }
        }
    }
}
