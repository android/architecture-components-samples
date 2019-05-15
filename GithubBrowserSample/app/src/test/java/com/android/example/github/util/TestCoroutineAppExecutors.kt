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
    val mainDispatcher = TestCoroutineDispatcher()
    val defaultDispatcher = TestCoroutineDispatcher()
    val ioDispatcher = TestCoroutineDispatcher()
    private val roomTransactionExecutor = CountingExecutor()
    private val roomQueryExecutor = CountingExecutor()

    private val allContexts = listOf(mainDispatcher, defaultDispatcher, ioDispatcher)
    private val executors = listOf(roomTransactionExecutor, roomQueryExecutor)
    val appExecutors = AppExecutors(
        mainThread = mainDispatcher,
        default = defaultDispatcher,
        io = ioDispatcher
    )

    fun setupRoom(builder: RoomDatabase.Builder<*>) {
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
        Dispatchers.setMain(mainDispatcher.coroutineDispatcher())
    }

    private fun CoroutineContext.coroutineDispatcher() =
        this[ContinuationInterceptor] as CoroutineDispatcher

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
            triggerAllActions()
            async.await()
        }
    }

    fun triggerAllActions() {
        do {
            // get current state signatures from Room's executors so that we
            // know if they execute anything in between
            val signatures = executors.map {
                it.createSnapshot()
            }
            // trigger all controlled actions
            allContexts.forEach {
                it.advanceUntilIdle()
            }
            // now check if all are idle + executors didn't do any work.
            val allIdle = allContexts.all {
                it.isIdle()
            } && executors.mapIndexed { index, executor ->
                executor.wasIdleSince(signatures[index])
            }.all {
                it
            }
        } while (!allIdle)
    }
}