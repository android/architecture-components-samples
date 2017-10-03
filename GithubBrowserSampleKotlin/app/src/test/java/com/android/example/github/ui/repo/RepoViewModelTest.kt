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

package com.android.example.github.ui.repo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import com.android.example.github.repository.RepoRepository
import com.android.example.github.vo.Contributor
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.*

@RunWith(JUnit4::class)
class RepoViewModelTest {

    @Suppress("unused")
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: RepoRepository
    private lateinit var repoViewModel: RepoViewModel

    @Before
    fun setup() {
        repository = mock()
        repoViewModel = RepoViewModel(repository)
    }

    @Test
    fun testNull() {
        assertThat<LiveData<Resource<Repo>>>(repoViewModel.repo, notNullValue())
        assertThat<LiveData<Resource<PagedList<Contributor>>>>(repoViewModel.contributors, notNullValue())
        verify<RepoRepository>(repository, never()).loadRepo(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun dontFetchWithoutObservers() {
        repoViewModel.setId("a", "b")
        verify<RepoRepository>(repository, never()).loadRepo(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun fetchWhenObserved() {

        val owner = ArgumentCaptor.forClass(String::class.java)
        val name = ArgumentCaptor.forClass(String::class.java)

        repoViewModel.setId("a", "b")
        repoViewModel.repo.observeForever(mock())

        verify<RepoRepository>(repository, times(1)).loadRepo(owner.capture(), name.capture())
        assertThat(owner.value, `is`("a"))
        assertThat(name.value, `is`("b"))
    }

    @Test
    fun changeWhileObserved() {
        val owner = ArgumentCaptor.forClass(String::class.java)
        val name = ArgumentCaptor.forClass(String::class.java)

        repoViewModel.repo.observeForever(mock())
        repoViewModel.setId("a", "b")
        repoViewModel.setId("c", "d")

        verify<RepoRepository>(repository, times(2)).loadRepo(owner.capture(), name.capture())
        assertThat(owner.allValues, `is`(Arrays.asList("a", "c")))
        assertThat(name.allValues, `is`(Arrays.asList("b", "d")))
    }

    @Test
    fun contributors() {
        val observer: Observer<Resource<PagedList<Contributor>>> = mock()

        repoViewModel.contributors.observeForever(observer)
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(repository)
        repoViewModel.setId("foo", "bar")
        verify<RepoRepository>(repository).loadContributors("foo", "bar")
    }

    @Test
    fun resetId() {
        val observer: Observer<RepoViewModel.RepoId> = mock()

        repoViewModel.repoId.observeForever(observer)
        verifyNoMoreInteractions(observer)
        repoViewModel.setId("foo", "bar")
        verify<Observer<RepoViewModel.RepoId>>(observer).onChanged(RepoViewModel.RepoId("foo", "bar"))
        reset<Observer<RepoViewModel.RepoId>>(observer)
        repoViewModel.setId("foo", "bar")
        verifyNoMoreInteractions(observer)
        repoViewModel.setId("a", "b")
        verify<Observer<RepoViewModel.RepoId>>(observer).onChanged(RepoViewModel.RepoId("a", "b"))
    }

    @Test
    fun retry() {
        repoViewModel.retry()
        verifyNoMoreInteractions(repository)
        repoViewModel.setId("foo", "bar")
        verifyNoMoreInteractions(repository)

        val observer: Observer<Resource<Repo>> = mock()
        repoViewModel.repo.observeForever(observer)

        verify<RepoRepository>(repository).loadRepo("foo", "bar")
        reset<RepoRepository>(repository)
        repoViewModel.retry()

        verify<RepoRepository>(repository).loadRepo("foo", "bar")
    }

    @Test
    fun nullRepoId() {
        repoViewModel.setId(null, null)
        val observer1: Observer<Resource<Repo>> = mock()
        val observer2: Observer<Resource<PagedList<Contributor>>> = mock()
        repoViewModel.repo.observeForever(observer1)
        repoViewModel.contributors.observeForever(observer2)

        verify<Observer<Resource<Repo>>>(observer1).onChanged(null)
        verify<Observer<Resource<PagedList<Contributor>>>>(observer2).onChanged(null)
    }
}