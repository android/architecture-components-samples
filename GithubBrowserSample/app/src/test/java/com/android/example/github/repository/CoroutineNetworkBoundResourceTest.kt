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

package com.android.example.github.repository


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.example.github.api.ApiResponse
import com.android.example.github.util.isIdle
import com.android.example.github.vo.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.Parameterized
import retrofit2.Response
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.ContinuationInterceptor

@ObsoleteCoroutinesApi// for test coroutine context
@ExperimentalCoroutinesApi // for Dispatchers.setMain
@RunWith(JUnit4::class)
class CoroutineNetworkBoundResourceTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testMainContext = TestCoroutineContext("test-main")

    private val testBackgroundContext = TestCoroutineContext("test-bg")

    private val dbData = MutableLiveData<Foo>()

    @Before
    fun init() {
        Dispatchers.setMain(testMainContext[ContinuationInterceptor.Key] as CoroutineDispatcher)
    }

    @After
    fun check() {
        triggerAllActions()
        assertThat(testMainContext.exceptions, `is`(emptyList()))
        assertThat(testBackgroundContext.exceptions, `is`(emptyList()))
    }

    @Test
    fun basicFromNetwork() {
        val saved = AtomicReference<Foo>()
        val liveData = networkBoundResource(
            saveCallResult = {
                withContext(testBackgroundContext) {
                    saved.set(it)
                    dbData.postValue(it)
                }
            },
            fetch = { ApiResponse.create(Response.success(Foo(1))) },
            loadFromDb = { dbData }
        )
        val collection = liveData.addObserver()
        triggerAllActions()
        collection.assertItems(
            Resource.loading(null)
        )
        dbData.value = null
        triggerAllActions()
        assertThat(saved.get(), `is`(Foo(1)))
        collection.assertItems(
            Resource.loading(null),
            Resource.success(Foo(1))
        )
    }

    @Test
    fun failureFromNetwork() {
        val body = ResponseBody.create(MediaType.parse("text/html"), "error")

        val liveData = networkBoundResource(
            saveCallResult = { error("shouldn't save") },
            fetch = { ApiResponse.create(Response.error<Foo>(500, body)) },
            loadFromDb = { dbData }
        )
        dbData.value = null
        liveData.addObserver().apply {
            triggerAllActions()
            assertItems(
                Resource.loading(null),
                Resource.error("error", null)
            )
        }
    }

    @Test
    fun dbSuccessWithoutNetwork() {
        val liveData = networkBoundResource<Foo, Foo>(
            saveCallResult = { error("nothing to save, it is from db") },
            shouldFetch = { false },
            fetch = { error("no reason to fetch") },
            loadFromDb = { dbData }
        )
        liveData.addObserver().apply {
            assertItems(Resource.loading(null))
            reset()
            dbData.value = Foo(1)
            triggerAllActions()
            assertItems(Resource.success(Foo(1)))
            reset()
            dbData.value = Foo(2)
            triggerAllActions()
            assertItems(Resource.success(Foo(2)))
        }
    }

    @Test
    fun dbSuccessWithFetchFailure() {
        val executeNetwork = CompletableDeferred<Unit>()
        val ld = networkBoundResource(
            saveCallResult = { error("should not try to save") },
            fetch = {
                executeNetwork.await()
                val body = ResponseBody.create(MediaType.parse("text/html"), "error")
                ApiResponse.create(Response.error<Foo>(400, body))
            },
            loadFromDb = { dbData },
            shouldFetch = { true }
        )
        ld.addObserver().apply {
            assertItems(Resource.loading(null))
            reset()
            dbData.value = Foo(1)
            triggerAllActions()
            assertItems(Resource.loading(Foo(1)))
            reset()
            executeNetwork.complete(Unit)
            triggerAllActions()
            assertItems(Resource.error("error", Foo(1)))
            reset()
            dbData.value = Foo(2)
            assertItems(Resource.error("error", Foo(2)))
        }
    }

    @Test
    fun dbSuccessWithReFetchSuccess() {
        val executeNetwork = CompletableDeferred<Unit>()
        val saved = AtomicReference<Foo>()
        val ld = networkBoundResource(
            saveCallResult = {
                assertThat(saved.compareAndSet(null, it), `is`(true))
                dbData.value = it
            },
            fetch = {
                executeNetwork.await()
                ApiResponse.create(Response.success(Foo(2)))
            },
            loadFromDb = { dbData },
            shouldFetch = { true }
        )
        ld.addObserver().apply {
            assertItems(Resource.loading(null))
            reset()
            dbData.value = Foo(1)
            triggerAllActions()
            assertItems(Resource.loading(Foo(1)))
            reset()
            executeNetwork.complete(Unit)
            triggerAllActions()
            assertItems(Resource.success(Foo(2)))
            assertThat(saved.get(), `is`(Foo(2)))
        }
    }

    @Test
    fun removeObserverWhileRunning() {
        val dbData = MutableLiveData<Foo>()
        val ld = networkBoundResource<Foo, Foo>(
            saveCallResult = {throw AssertionError("should not call")},
            fetch = {throw AssertionError("should not call")},
            loadFromDb = { dbData }
        )
        ld.addObserver().apply {
            assertItems(Resource.loading(null))
            assertThat(dbData.hasObservers(), `is`(true))
            unsubscribe()
            advanceTimeBy(10_000)
            assertThat(dbData.hasObservers(), `is`(false))
            assertItems(Resource.loading(null))
        }
    }

    private fun triggerAllActions() {
        do {
            testMainContext.triggerActions()
            testBackgroundContext.triggerActions()
            val allIdle = listOf(testMainContext, testBackgroundContext).all {
                it.isIdle()
            }
        } while (!allIdle)
    }

    private fun advanceTimeBy(time: Long) {
        testMainContext.advanceTimeBy(time)
        testBackgroundContext.advanceTimeBy(time)
        triggerAllActions()
    }

    private data class Foo(var value: Int)

    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun param(): List<Boolean> {
            return arrayListOf(true, false)
        }
    }

    private fun <T> runOnMain(block: () -> T): T {
        return runBlocking {
            val async = async(Dispatchers.Main) {
                block()
            }
            testMainContext.triggerActions()
            async.await()
        }
    }


    private fun <T> LiveData<T>.addObserver(): CollectingObserver<T> {
        return runOnMain {
            val observer = CollectingObserver(this)
            observeForever(observer)
            observer
        }
    }

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

        fun unsubscribe() = runOnMain {
            liveData.removeObserver(this)
        }

        fun reset() = runOnMain {
            items.clear()
        }
    }
}