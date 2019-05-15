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
import androidx.lifecycle.MutableLiveData
import com.android.example.github.api.ApiResponse
import com.android.example.github.util.CoroutineTestBase
import com.android.example.github.vo.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.Parameterized
import retrofit2.Response
import java.util.concurrent.atomic.AtomicReference

@ExperimentalCoroutinesApi // for Dispatchers.setMain
@RunWith(JUnit4::class)
class CoroutineNetworkBoundResourceTest : CoroutineTestBase() {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dbData = MutableLiveData<Foo>()

    @Test
    fun basicFromNetwork() {
        val saved = AtomicReference<Foo>()
        val liveData = networkBoundResource(
            saveCallResult = {
                withContext(testExecutors.defaultContext) {
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
            saveCallResult = { throw AssertionError("should not call") },
            fetch = { throw AssertionError("should not call") },
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

    private data class Foo(var value: Int)

    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun param(): List<Boolean> {
            return arrayListOf(true, false)
        }
    }
}