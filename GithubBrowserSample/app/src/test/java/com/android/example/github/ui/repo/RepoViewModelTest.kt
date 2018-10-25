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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.android.example.github.repository.RepoRepository
import com.android.example.github.util.mock
import com.android.example.github.vo.Contributor
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

@RunWith(JUnit4::class)
class RepoViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock(RepoRepository::class.java)
    private var repoViewModel = RepoViewModel(repository)

    @Test
    fun testNull() {
        assertThat(repoViewModel.repo, notNullValue())
        assertThat(repoViewModel.contributors, notNullValue())
        verify(repository, never()).loadRepo(anyString(), anyString())
    }

    @Test
    fun dontFetchWithoutObservers() {
        repoViewModel.setId("a", "b")
        verify(repository, never()).loadRepo(anyString(), anyString())
    }

    @Test
    fun fetchWhenObserved() {
        repoViewModel.setId("a", "b")
        repoViewModel.repo.observeForever(mock())
        verify(repository).loadRepo("a", "b")
    }

    @Test
    fun changeWhileObserved() {
        repoViewModel.repo.observeForever(mock())

        repoViewModel.setId("a", "b")
        repoViewModel.setId("c", "d")

        verify(repository).loadRepo("a", "b")
        verify(repository).loadRepo("c", "d")
    }

    @Test
    fun contributors() {
        val observer = mock<Observer<Resource<List<Contributor>>>>()
        repoViewModel.contributors.observeForever(observer)
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(repository)
        repoViewModel.setId("foo", "bar")
        verify(repository).loadContributors("foo", "bar")
    }

    @Test
    fun resetId() {
        val observer = mock<Observer<RepoViewModel.RepoId>>()
        repoViewModel.repoId.observeForever(observer)
        verifyNoMoreInteractions(observer)
        repoViewModel.setId("foo", "bar")
        verify(observer).onChanged(RepoViewModel.RepoId("foo", "bar"))
        reset(observer)
        repoViewModel.setId("foo", "bar")
        verifyNoMoreInteractions(observer)
        repoViewModel.setId("a", "b")
        verify(observer).onChanged(RepoViewModel.RepoId("a", "b"))
    }

    @Test
    fun retry() {
        repoViewModel.retry()
        verifyNoMoreInteractions(repository)
        repoViewModel.setId("foo", "bar")
        verifyNoMoreInteractions(repository)
        val observer = mock<Observer<Resource<Repo>>>()
        repoViewModel.repo.observeForever(observer)
        verify(repository).loadRepo("foo", "bar")
        reset(repository)
        repoViewModel.retry()
        verify(repository).loadRepo("foo", "bar")
    }

    @Test
    fun blankRepoId() {
        repoViewModel.setId("", "")
        val observer1 = mock<Observer<Resource<Repo>>>()
        val observer2 = mock<Observer<Resource<List<Contributor>>>>()
        repoViewModel.repo.observeForever(observer1)
        repoViewModel.contributors.observeForever(observer2)
        verify(observer1).onChanged(null)
        verify(observer2).onChanged(null)
    }
}