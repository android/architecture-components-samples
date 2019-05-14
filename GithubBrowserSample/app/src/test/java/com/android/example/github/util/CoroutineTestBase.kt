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

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import kotlin.coroutines.ContinuationInterceptor

@ObsoleteCoroutinesApi // TestDispatchers
open class CoroutineTestBase {
    @JvmField
    @Rule
    val testExecutors = TestCoroutineAppExecutors()

    fun <T> LiveData<T>.addObserver(): CollectingObserver<T> {
        return testExecutors.runOnMain {
            val observer = CollectingObserver(this)
            observeForever(observer)
            observer
        }
    }

    fun triggerAllActions() = testExecutors.triggerAllActions()

    inner class CollectingObserver<T>(
        private val liveData: LiveData<T>
    ) : Observer<T> {
        private var items = mutableListOf<T>()
        override fun onChanged(t: T) {
            items.add(t)
        }

        fun assertItems(vararg expected: T) {
            MatcherAssert.assertThat(items, CoreMatchers.`is`(expected.asList()))
        }

        fun unsubscribe() = testExecutors.runOnMain {
            liveData.removeObserver(this)
        }

        fun reset() = testExecutors.runOnMain {
            items.clear()
        }
    }
}