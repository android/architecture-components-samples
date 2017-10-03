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

package com.android.example.github.ui.user

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.android.example.github.repository.RepoRepository
import com.android.example.github.repository.UserRepository
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.User
import com.nhaarman.mockito_kotlin.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers

@RunWith(JUnit4::class)
class UserViewModelTest {
    @Suppress("unused")
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var repoRepository: RepoRepository

    @Before
    fun setup() {
        userRepository = mock()
        repoRepository = mock()
        userViewModel = UserViewModel(userRepository, repoRepository)
    }

    @Test
    fun testNull() {
        assertThat(userViewModel.user, notNullValue())
        verify(userRepository, never()).loadUser(ArgumentMatchers.anyString())
        userViewModel.setLogin("foo")
        verify(userRepository, never()).loadUser(ArgumentMatchers.anyString())
    }

    @Test
    fun testCallRepo() {

        val captor = argumentCaptor<String>()

        userViewModel.user.observeForever(mock())
        userViewModel.setLogin("abc")

        com.nhaarman.mockito_kotlin.verify(userRepository).loadUser(captor.capture())

        assertThat(captor.lastValue, `is`("abc"))
        com.nhaarman.mockito_kotlin.reset(userRepository)
        userViewModel.setLogin("ddd")
        com.nhaarman.mockito_kotlin.verify(userRepository).loadUser(captor.capture())
        assertThat(captor.lastValue, `is`("ddd"))
    }

    @Test
    fun sendResultToUI() {
        val foo = MutableLiveData<Resource<User>>()
        val bar = MutableLiveData<Resource<User>>()
        whenever(userRepository.loadUser("foo")).thenReturn(foo)
        whenever(userRepository.loadUser("bar")).thenReturn(bar)
        mock<Observer<Resource<User>>>().apply {
            userViewModel.user.observeForever(this)
            userViewModel.setLogin("foo")
            verify(this, never()).onChanged(ArgumentMatchers.any())
            val fooUser = TestUtil.createUser("foo")
            val fooValue = Resource.success(fooUser)

            foo.value = fooValue
            verify(this).onChanged(fooValue)
            reset(this)
            val barUser = TestUtil.createUser("bar")
            val barValue = Resource.success(barUser)
            bar.value = barValue
            userViewModel.setLogin("bar")
            verify(this).onChanged(barValue)
        }
    }

    @Test
    fun loadRepositories() {
        userViewModel.repositories.observeForever(mock())
        verifyNoMoreInteractions(repoRepository)
        userViewModel.setLogin("foo")
        verify(repoRepository).loadRepos("foo")
        reset(repoRepository)
        userViewModel.setLogin("bar")
        verify(repoRepository).loadRepos("bar")
        verifyNoMoreInteractions(userRepository)
    }

    @Test
    fun retry() {
        userViewModel.setLogin("foo")
        verifyNoMoreInteractions(repoRepository, userRepository)
        userViewModel.retry()
        verifyNoMoreInteractions(repoRepository, userRepository)
        val userObserver: Observer<Resource<User>> = mock()

        userViewModel.user.observeForever(userObserver)

        val repoObserver: Observer<Resource<List<Repo>>> = mock()
        userViewModel.repositories.observeForever(repoObserver)

        verify(userRepository).loadUser("foo")
        verify(repoRepository).loadRepos("foo")
        reset(userRepository, repoRepository)

        userViewModel.retry()
        verify(userRepository).loadUser("foo")
        verify(repoRepository).loadRepos("foo")
        reset(userRepository, repoRepository)

        userViewModel.user.removeObserver(userObserver)
        userViewModel.repositories.removeObserver(repoObserver)

        userViewModel.retry()
        verifyNoMoreInteractions(userRepository, repoRepository)
    }

    @Test
    fun nullUser() {
        val observer: Observer<Resource<User>> = mock()
        userViewModel.setLogin("foo")
        userViewModel.setLogin(null)
        userViewModel.user.observeForever(observer)
        verify(observer).onChanged(null)
    }

    @Test
    fun nullRepoList() {
        val observer: Observer<Resource<List<Repo>>> = mock()
        userViewModel.setLogin("foo")
        userViewModel.setLogin(null)
        userViewModel.repositories.observeForever(observer)
        verify(observer).onChanged(null)
    }

    @Test
    fun dontRefreshOnSameData() {
        val observer: Observer<String> = mock()
        userViewModel.login.observeForever(observer)
        verifyNoMoreInteractions(observer)
        userViewModel.setLogin("foo")
        verify(observer).onChanged("foo")
        reset(observer)
        userViewModel.setLogin("foo")
        verifyNoMoreInteractions(observer)
        userViewModel.setLogin("bar")
        verify(observer).onChanged("bar")
    }

    @Test
    fun noRetryWithoutUser() {
        userViewModel.retry()
        verifyNoMoreInteractions(userRepository, repoRepository)
    }
}