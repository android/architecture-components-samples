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

package com.android.example.github_kotlin.ui.search

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.android.example.github_kotlin.repository.RepoRepository
import com.android.example.github_kotlin.vo.Repo
import com.android.example.github_kotlin.vo.Resource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*

@RunWith(JUnit4::class)
class SearchViewModelTest {
    @Suppress("unused")
    @get:Rule
    var instantExecutor = InstantTaskExecutorRule()
    private lateinit var viewModel: SearchViewModel
    private lateinit var repository: RepoRepository
    @Before
    fun init() {
        repository = mock()
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun empty() {
        val result: Observer<Resource<List<Repo>>> = mock()
        viewModel.results.observeForever(result)
        viewModel.loadNextPage()
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun basic() {
        val result: Observer<Resource<List<Repo>>> = mock()
        viewModel.results.observeForever(result)
        viewModel.setQuery("foo")
        verify<RepoRepository>(repository).search("foo")
        verify<RepoRepository>(repository, never()).searchNextPage("foo")
    }

    @Test
    fun noObserverNoQuery() {
        whenever(repository.searchNextPage("foo")).thenReturn(mock())
        viewModel.setQuery("foo")
        verify<RepoRepository>(repository, never()).search("foo")
        // next page is user interaction and even if loading state is not observed, we query
        // would be better to avoid that if main search query is not observed
        viewModel.loadNextPage()
        verify<RepoRepository>(repository).searchNextPage("foo")
    }

    @Test
    fun swap() {
        val nextPage = MutableLiveData<Resource<Boolean>>()
        whenever(repository.searchNextPage("foo")).thenReturn(nextPage)

        val result: Observer<Resource<List<Repo>>> = mock()
        viewModel.results.observeForever(result)
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("foo")
        verify<RepoRepository>(repository).search("foo")
        viewModel.loadNextPage()

        viewModel.loadMoreStatus.observeForever(mock())
        verify<RepoRepository>(repository).searchNextPage("foo")
        assertThat(nextPage.hasActiveObservers(), `is`(true))
        viewModel.setQuery("bar")
        assertThat(nextPage.hasActiveObservers(), `is`(false))
        verify<RepoRepository>(repository).search("bar")
        verify<RepoRepository>(repository, never()).searchNextPage("bar")
    }

    @Test
    fun refresh() {
        viewModel.refresh()
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("foo")
        viewModel.refresh()
        verifyNoMoreInteractions(repository)
        viewModel.results.observeForever(mock())
        verify<RepoRepository>(repository).search("foo")
        reset<RepoRepository>(repository)
        viewModel.refresh()
        verify<RepoRepository>(repository).search("foo")
    }

    @Test
    fun resetSameQuery() {
        viewModel.results.observeForever(mock())
        viewModel.setQuery("foo")
        verify<RepoRepository>(repository).search("foo")
        reset<RepoRepository>(repository)
        viewModel.setQuery("FOO")
        verifyNoMoreInteractions(repository)
        viewModel.setQuery("bar")
        verify<RepoRepository>(repository).search("bar")
    }
}