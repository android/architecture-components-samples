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

class CountingExecutor : Executor {
    val count = AtomicInteger()
    fun isIdle() = count.get() == 0
    private val delegate by lazy {
        Executors.newSingleThreadExecutor()
    }

    override fun execute(command: Runnable) {
        count.incrementAndGet()
        delegate.submit {
            try {
                command.run()
            } finally {
                count.decrementAndGet()
            }
        }
    }

}