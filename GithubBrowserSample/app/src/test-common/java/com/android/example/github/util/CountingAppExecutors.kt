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

package com.android.example.github.util

import com.android.example.github.AppExecutors
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CountingAppExecutors(idleCallback: (() -> Unit)? = null) {

    private val counterLock = ReentrantLock()
    private val queueEmpty = counterLock.newCondition()
    private var taskCount = 0

    val appExecutors: AppExecutors

    init {
        val increment: () -> Unit = {
            counterLock.withLock {
                taskCount++
            }
        }
        val decrement: () -> Unit = {
            val unlock = counterLock.withLock {
                taskCount --
                if(taskCount == 0) {
                    queueEmpty.signalAll()
                    true
                } else {
                    false
                }

            }
            if (unlock) {
                idleCallback?.let {
                    it.invoke()
                }
            }


        }
        appExecutors = AppExecutors(
            CountingExecutor(increment, decrement).asCoroutineDispatcher(),
            CountingExecutor(increment, decrement).asCoroutineDispatcher(),
            CountingExecutor(increment, decrement).asCoroutineDispatcher()
        )
    }

    fun taskCount() = counterLock.withLock {
        taskCount
    }

    fun drainTasks(time: Int, timeUnit: TimeUnit) {
        val end = System.currentTimeMillis() + timeUnit.toMillis(time.toLong())
        while (true) {
            counterLock.withLock {
                if (taskCount == 0) {
                    return
                }
                val now = System.currentTimeMillis()
                val remaining = end - now
                if (remaining > 0) {
                    queueEmpty.await(remaining, TimeUnit.MILLISECONDS)
                } else {
                    throw TimeoutException("could not drain tasks")
                }
            }
        }
    }

    private class CountingExecutor(
        private val increment: () -> Unit,
        private val decrement: () -> Unit
    ) : Executor {

        private val delegate = Executors.newSingleThreadExecutor()

        override fun execute(command: Runnable) {
            increment()
            delegate.execute {
                try {
                    command.run()
                } finally {
                    decrement()
                }
            }
        }
    }
}
