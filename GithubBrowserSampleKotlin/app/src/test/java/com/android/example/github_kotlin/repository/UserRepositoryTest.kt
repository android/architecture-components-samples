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
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.android.example.github_kotlin.api.GithubService
import com.android.example.github_kotlin.db.UserDao
import com.android.example.github_kotlin.util.InstantAppExecutors
import com.android.example.github_kotlin.util.TestUtil
import com.android.example.github_kotlin.util.successCall
import com.android.example.github_kotlin.vo.Resource
import com.android.example.github_kotlin.vo.User
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserRepositoryTest {
    private lateinit var userDao: UserDao
    private lateinit var githubService: GithubService
    private lateinit var repo: UserRepository

    @Suppress("unused")
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        userDao = mock()
        githubService = mock()
        repo = UserRepository(InstantAppExecutors(), userDao, githubService)
    }

    @Test
    fun loadUser() {
        repo.loadUser("abc")
        verify(userDao).findByLogin("abc")
    }

    @Test
    fun goToNetwork() {
        val dbData = MutableLiveData<User>()
        whenever(userDao.findByLogin("foo")).thenReturn(dbData)
        val user = TestUtil.createUser("foo")
        val call = successCall(user)
        whenever(githubService.getUser("foo")).thenReturn(call)
        val observer: Observer<Resource<User>> = mock()

        repo.loadUser("foo").observeForever(observer)
        verify(githubService, never()).getUser("foo")
        val updatedDbData = MutableLiveData<User>()
        whenever(userDao.findByLogin("foo")).thenReturn(updatedDbData)
        dbData.value = null
        verify(githubService).getUser("foo")
    }

    @Test
    fun dontGoToNetwork() {
        val dbData = MutableLiveData<User>()
        val user = TestUtil.createUser("foo")
        dbData.value = user
        whenever(userDao.findByLogin("foo")).thenReturn(dbData)
        val observer: Observer<Resource<User>> = mock()
        repo.loadUser("foo").observeForever(observer)
        verify(githubService, never()).getUser("foo")
        verify(observer).onChanged(Resource.success(user))
    }
}