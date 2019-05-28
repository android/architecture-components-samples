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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

// Utility class to observe TestCoroutineContext internals until the new TestCoroutineContext
// APIs are available
@ExperimentalCoroutinesApi
fun TestCoroutineDispatcher.isIdle(): Boolean {
    val queueField = this::class.java
        .getDeclaredField("queue")
    queueField.isAccessible = true
    val queue = queueField.get(this)
    val peekMethod = queue::class.java
        .getDeclaredMethod("peek")
    val nextTask = peekMethod.invoke(queue) ?: return true
    val timeField = nextTask::class.java.getDeclaredField("time")
    timeField.isAccessible = true
    val time = timeField.getLong(nextTask)
    return time > currentTime
}