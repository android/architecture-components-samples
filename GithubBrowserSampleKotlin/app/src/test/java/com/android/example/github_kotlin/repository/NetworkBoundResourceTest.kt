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

package com.android.example.github_kotlin.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.android.example.github_kotlin.api.ApiResponse
import com.android.example.github_kotlin.util.CountingAppExecutors
import com.android.example.github_kotlin.util.InstantAppExecutors
import com.android.example.github_kotlin.util.createCall
import com.android.example.github_kotlin.vo.Resource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@RunWith(Parameterized::class)
class NetworkBoundResourceTest(private val useRealExecutors: Boolean) {
    @Suppress("unused")
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveCallResult: (Foo) -> Unit

    private lateinit var shouldFetch: (Foo?) -> Boolean

    private lateinit var createCall: () -> LiveData<ApiResponse<Foo>>

    private val dbData = MutableLiveData<Foo>()

    private lateinit var networkBoundResource: NetworkBoundResource<Foo, Foo>

    private val fetchedOnce = AtomicBoolean(false)
    private lateinit var countingAppExecutors: CountingAppExecutors

    init {
        if (useRealExecutors) {
            countingAppExecutors = CountingAppExecutors()
        }
    }

    @Before
    fun init() {
        val appExecutors = when {
            useRealExecutors -> countingAppExecutors.appExecutors
            else -> InstantAppExecutors()
        }

        networkBoundResource = object : NetworkBoundResource<Foo, Foo>(appExecutors) {
            override fun saveCallResult(item: Foo) {
                saveCallResult.invoke(item)
            }

            // since test methods don't handle repetitive fetching, call it only once
            override fun shouldFetch(data: Foo?): Boolean =
                    shouldFetch.invoke(data) && fetchedOnce.compareAndSet(false, true)

            override fun loadFromDb(): LiveData<Foo> {
                return dbData
            }

            override fun createCall(): LiveData<ApiResponse<Foo>> = createCall.invoke()
        }
    }

    private fun drain() {
        if (!useRealExecutors) {
            return
        }
        try {
            countingAppExecutors.drainTasks(1, TimeUnit.SECONDS)
        } catch (t: Throwable) {
            throw AssertionError(t)
        }

    }

    @Test
    fun basicFromNetwork() {
        val saved = AtomicReference<Foo>()
        shouldFetch = { Objects.isNull(it) }
        val fetchedDbValue = Foo(1)
        saveCallResult = {
            saved.set(it)
            dbData.setValue(fetchedDbValue)
        }
        val networkResult = Foo(1)
        createCall = { createCall(Response.success(networkResult)) }

        val observer: Observer<Resource<Foo>> = mock()

        networkBoundResource.asLiveData().observeForever(observer)
        drain()
        verify(observer).onChanged(Resource.loading<Foo>(null))
        reset(observer)
        dbData.value = null
        drain()
        assertThat(saved.get(), `is`(networkResult))
        verify(observer).onChanged(Resource.success(fetchedDbValue))
    }

    @Test
    fun failureFromNetwork() {
        val saved = AtomicBoolean(false)
        shouldFetch = { Objects.isNull(it) }
        saveCallResult = {
            saved.set(true)
        }
        val body = ResponseBody.create(MediaType.parse("text/html"), "error")
        createCall = { createCall(Response.error<Foo>(500, body)) }

        val observer: Observer<Resource<Foo>> = mock()
        networkBoundResource.asLiveData().observeForever(observer)
        drain()
        verify(observer).onChanged(Resource.loading<Foo>(null))
        reset(observer)
        dbData.value = null
        drain()
        assertThat(saved.get(), `is`(false))
        verify(observer).onChanged(Resource.error<Foo>("error", null))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun dbSuccessWithoutNetwork() {
        val saved = AtomicBoolean(false)
        shouldFetch = { Objects.isNull(it) }
        saveCallResult = {
            saved.set(true)
        }

        val observer: Observer<Resource<Foo>> = mock()
        networkBoundResource.asLiveData().observeForever(observer)
        drain()
        verify(observer).onChanged(Resource.loading<Foo>(null))
        reset(observer)
        val dbFoo = Foo(1)
        dbData.value = dbFoo
        drain()
        verify(observer).onChanged(Resource.success(dbFoo))
        assertThat(saved.get(), `is`(false))
        val dbFoo2 = Foo(2)
        dbData.value = dbFoo2
        drain()
        verify(observer).onChanged(Resource.success(dbFoo2))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun dbSuccessWithFetchFailure() {
        val dbValue = Foo(1)
        val saved = AtomicBoolean(false)

        shouldFetch = { foo -> foo === dbValue }
        saveCallResult = { saved.set(true) }

        val body = ResponseBody.create(MediaType.parse("text/html"), "error")
        val apiResponseLiveData = MutableLiveData<ApiResponse<Foo>>()

        createCall = { apiResponseLiveData }

        val observer: Observer<Resource<Foo>> = mock()
        networkBoundResource.asLiveData().observeForever(observer)
        drain()
        verify(observer).onChanged(Resource.loading<Foo>(null))
        reset(observer)

        dbData.value = dbValue
        drain()
        verify(observer).onChanged(Resource.loading(dbValue))

        apiResponseLiveData.value = ApiResponse(Response.error<Foo>(400, body))
        drain()
        assertThat(saved.get(), `is`(false))
        verify(observer).onChanged(Resource.error("error", dbValue))

        val dbValue2 = Foo(2)
        dbData.value = dbValue2
        drain()
        verify(observer).onChanged(Resource.error("error", dbValue2))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun dbSuccessWithReFetchSuccess() {
        val dbValue = Foo(1)
        val dbValue2 = Foo(2)
        val saved = AtomicReference<Foo>()

        shouldFetch = { foo -> foo === dbValue }
        saveCallResult = { foo ->
            saved.set(foo)
            dbData.setValue(dbValue2)
        }

        val apiResponseLiveData = MutableLiveData<ApiResponse<Foo>>()
        createCall = { apiResponseLiveData }

        val observer: Observer<Resource<Foo>> = mock()
        networkBoundResource.asLiveData().observeForever(observer)
        drain()
        verify(observer).onChanged(Resource.loading<Foo>(null))
        reset(observer)

        dbData.value = dbValue
        drain()
        val networkResult = Foo(1)
        verify(observer).onChanged(Resource.loading(dbValue))
        apiResponseLiveData.value = ApiResponse(Response.success(networkResult))
        drain()
        assertThat(saved.get(), `is`(networkResult))
        verify(observer).onChanged(Resource.success(dbValue2))
        verifyNoMoreInteractions(observer)
    }

    @Suppress("unused")
    internal class Foo(var value: Int)

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun param(): List<Boolean> {
            return Arrays.asList(true, false)
        }
    }
}