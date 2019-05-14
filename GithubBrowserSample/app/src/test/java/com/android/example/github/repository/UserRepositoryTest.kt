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

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.android.example.github.api.ApiResponse
import com.android.example.github.api.FakeGithubService
import com.android.example.github.db.GithubDb
import com.android.example.github.db.UserDao
import com.android.example.github.util.CoroutineTestBase
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class UserRepositoryTest : CoroutineTestBase() {
    private val githubService = FakeGithubService()
    private lateinit var userDao: UserDao
    private lateinit var repo: UserRepository

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, GithubDb::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        repo = UserRepository(userDao, githubService)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun goToNetwork() {
        val user = TestUtil.createUser("foo")
        val calledService = CompletableDeferred<Unit>()
        runBlocking {
            githubService.getUserImpl = {
                calledService.complete(Unit)
                ApiResponse.create(Response.success(user))
            }
            repo.loadUser("foo").addObserver().apply {
                calledService.await()
                testExecutors.triggerAllActions()
                assertItems(
                    Resource.loading(null),
                    Resource.success(user)
                )
            }
            // check it is in the db
            assertThat(userDao.getByLogin("foo"), `is`(user))
        }

    }

    @Test
    fun dontGoToNetwork() {
        val user = TestUtil.createUser("foo")
        runBlocking {
            userDao.insert(user)
            repo.loadUser("foo").addObserver().apply {
                assertItems(
                    Resource.loading(null),
                    Resource.success(user)
                )
            }
        }
    }
}