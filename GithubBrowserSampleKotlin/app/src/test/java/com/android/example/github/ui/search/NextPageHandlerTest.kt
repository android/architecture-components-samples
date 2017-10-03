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

package com.android.example.github.ui.search

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import com.android.example.github.repository.RepoRepository
import com.android.example.github.vo.Resource
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class NextPageHandlerTest {

    @Suppress("unused")
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var pageHandler: SearchViewModel.Companion.NextPageHandler

    private lateinit var repository: RepoRepository

    private val status: SearchViewModel.LoadMoreState?
        get() = pageHandler.loadMoreState.value

    @Before
    fun init() {
        repository = mock(RepoRepository::class.java)
        pageHandler = SearchViewModel.Companion.NextPageHandler(repository)
    }

    @Test
    fun constructor() {
        val initial = status
        assertThat<SearchViewModel.LoadMoreState>(initial, notNullValue())
        assertThat(initial!!.isRunning, `is`(false))
        assertThat<String>(initial.errorMessage, nullValue())
    }

    @Test
    fun reloadSameValue() {
        enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        verify<RepoRepository>(repository).searchNextPage("foo")

        reset<RepoRepository>(repository)
        pageHandler.queryNextPage("foo")
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun success() {
        val liveData = enqueueResponse("foo")

        pageHandler.queryNextPage("foo")
        verify<RepoRepository>(repository).searchNextPage("foo")
        assertThat(liveData.hasActiveObservers(), `is`(true))
        pageHandler.onChanged(Resource.loading<Boolean>(null))
        assertThat(liveData.hasActiveObservers(), `is`(true))
        assertThat(status!!.isRunning, `is`(true))

        pageHandler.onChanged(Resource.success(true))
        assertThat(liveData.hasActiveObservers(), `is`(false))
        assertThat(pageHandler.hasMore, `is`(true))
        assertThat(status!!.isRunning, `is`(false))
        assertThat(liveData.hasActiveObservers(), `is`(false))

        // requery
        reset<RepoRepository>(repository)
        val nextPage = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        verify<RepoRepository>(repository).searchNextPage("foo")
        assertThat(nextPage.hasActiveObservers(), `is`(true))

        pageHandler.onChanged(Resource.success(false))
        assertThat(liveData.hasActiveObservers(), `is`(false))
        assertThat(pageHandler.hasMore, `is`(false))
        assertThat(status!!.isRunning, `is`(false))
        assertThat(nextPage.hasActiveObservers(), `is`(false))

        // retry, no query
        reset<RepoRepository>(repository)
        pageHandler.queryNextPage("foo")
        verifyNoMoreInteractions(repository)
        pageHandler.queryNextPage("foo")
        verifyNoMoreInteractions(repository)

        // query another
        val bar = enqueueResponse("bar")
        pageHandler.queryNextPage("bar")
        verify<RepoRepository>(repository).searchNextPage("bar")
        assertThat(bar.hasActiveObservers(), `is`(true))
    }

    @Test
    fun failure() {
        val liveData = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        assertThat(liveData.hasActiveObservers(), `is`(true))
        pageHandler.onChanged(Resource.error("idk", false))
        assertThat(liveData.hasActiveObservers(), `is`(false))
        assertThat<String>(status!!.errorMessage, `is`("idk"))
        assertThat<String>(status!!.errorMessageIfNotHandled, `is`("idk"))
        assertThat<String>(status!!.errorMessageIfNotHandled, nullValue())
        assertThat(status!!.isRunning, `is`(false))
        assertThat(pageHandler.hasMore, `is`(true))

        reset<RepoRepository>(repository)
        val liveData2 = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        assertThat(liveData2.hasActiveObservers(), `is`(true))
        assertThat(status!!.isRunning, `is`(true))
        pageHandler.onChanged(Resource.success(false))
        assertThat(status!!.isRunning, `is`(false))
        assertThat<String>(status!!.errorMessage, `is`(nullValue()))
        assertThat(pageHandler.hasMore, `is`(false))
    }

    @Test
    fun nullOnChanged() {
        val liveData = enqueueResponse("foo")
        pageHandler.queryNextPage("foo")
        assertThat(liveData.hasActiveObservers(), `is`(true))
        pageHandler.onChanged(null)
        assertThat(liveData.hasActiveObservers(), `is`(false))
    }

    private fun enqueueResponse(query: String): MutableLiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        whenever(repository.searchNextPage(query)).thenReturn(liveData)
        return liveData
    }
}