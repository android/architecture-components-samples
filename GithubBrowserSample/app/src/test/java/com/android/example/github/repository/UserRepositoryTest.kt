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

package com.android.example.github.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.android.example.github.api.ApiResponse
import com.android.example.github.api.FakeGithubService
import com.android.example.github.db.UserDao
import com.android.example.github.util.CoroutineTestBase
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.Resource
import com.android.example.github.vo.User
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

@ObsoleteCoroutinesApi
@RunWith(JUnit4::class)
class UserRepositoryTest : CoroutineTestBase() {
    private val userDao = mock(UserDao::class.java)
    private val githubService = spy(FakeGithubService())
    private val repo = UserRepository(userDao, githubService)

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun loadUser() {
        `when`(userDao!!.findByLogin("abc")).thenReturn(MutableLiveData())
        repo.loadUser("abc").addObserver()
        verify(userDao).findByLogin("abc")
    }

    @Test
    fun goToNetwork() {
        val dbData = MutableLiveData<User>()
        val user = TestUtil.createUser("foo")
        `when`(userDao!!.findByLogin("foo"))
            .thenReturn(dbData) // first controlled value
            .thenReturn(MutableLiveData<User>(user)) // then updated value
            .thenThrow(AssertionError("unexpected call"))
        val calledService = AtomicBoolean()
        githubService.getUserImpl = {
            calledService.set(true)
            ApiResponse.create(Response.success(user))
        }
        repo.loadUser("foo").addObserver().apply {
            assertItems(Resource.loading(null))
            reset()
            dbData.value = null
            triggerAllActions()
            assertItems(Resource.success(user))
            MatcherAssert.assertThat(calledService.get(), CoreMatchers.`is`(true))
        }
    }

    @Test
    fun dontGoToNetwork() {
        val user = TestUtil.createUser("foo")
        val dbData = MutableLiveData<User>(user)
        `when`(userDao!!.findByLogin("foo"))
            .thenReturn(dbData)
            .thenThrow(AssertionError("unexpected db read call"))
        repo.loadUser("foo").addObserver().apply {
            triggerAllActions()
            assertItems(
                Resource.loading(null),
                Resource.success(user)
            )
        }
    }
}