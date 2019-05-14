/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.room.RoomDatabase
import com.android.example.github.AppExecutors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class TestCoroutineAppExecutors : TestWatcher() {
    val mainContext = TestCoroutineDispatcher()
    val defaultContext = TestCoroutineDispatcher()
    val ioContext = TestCoroutineDispatcher()
    // initialized lazily
    var roomTransactionExecutor = CountingExecutor()
    var roomQueryExecutor = CountingExecutor()

    private val allContexts = listOf(mainContext, defaultContext, ioContext)

    val appExecutors = AppExecutors(
        mainThread = mainContext,
        default = defaultContext,
        io = ioContext
    )

    fun setupRoom(builder : RoomDatabase.Builder<*>) {
        builder.setQueryExecutor(roomQueryExecutor)
        builder.setTransactionExecutor(roomTransactionExecutor)
    }

    fun advanceTimeBy(time: Long) {
        allContexts.forEach {
            it.advanceTimeBy(time)
        }
        triggerAllActions()
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(mainContext.coroutineDispatcher())
    }

    private fun CoroutineContext.coroutineDispatcher() = this[ContinuationInterceptor] as CoroutineDispatcher

    override fun finished(description: Description?) {
        super.finished(description)
        triggerAllActions()
        Dispatchers.resetMain()
    }

    fun <T> runOnMain(block: () -> T): T {
        return runBlocking {
            val async = async(Dispatchers.Main) {
                block()
            }
            //TODO which one?
            // testMainContext.triggerActions()
            triggerAllActions()
            async.await()
        }
    }

    fun triggerAllActions() {
        do {
            allContexts.forEach {
                it.advanceUntilIdle()
            }
            val allIdle = allContexts.all {
                it.isIdle()
            }
        } while (!allIdle || !roomTransactionExecutor.isIdle() ||
                !roomQueryExecutor.isIdle())
    }
}
