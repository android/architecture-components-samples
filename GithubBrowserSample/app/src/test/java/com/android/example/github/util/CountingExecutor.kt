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

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class Snapshot(
    private val executor: CountingExecutor,
    val actionCount: Int
) {
    fun isStillValid(): Boolean {
        return executor.wasIdleSince(this)
    }
}

/**
 * Simple [Executor] that uses a real thread but also counts # of active runnables.
 */
class CountingExecutor : Executor {
    // # of state changes. This allows ensuring that it was idle between two calls, mainly to
    // know whether anything new was scheduled after we've synced the controlled executors.
    private val actionCount = AtomicInteger()

    /**
     * Returns true if this Executor didn't have any change (enqueued tasks or finished tasks)
     * between now and the provided [snapshot] which is received from [createSnapshot].
     */
    fun wasIdleSince(snapshot: Snapshot): Boolean {
        val count = actionCount.get()
        return count % 2 == 0 && snapshot.actionCount == count
    }

    private val delegate by lazy {
        Executors.newSingleThreadExecutor()
    }

    fun createSnapshot() = Snapshot(this, actionCount.get())

    override fun execute(command: Runnable) {
        actionCount.incrementAndGet()
        delegate.submit {
            try {
                command.run()
            } finally {
                actionCount.incrementAndGet()
            }
        }
    }
}