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
import com.android.example.github.api.RepoSearchResponse
import com.android.example.github.db.GithubDb
import com.android.example.github.db.RepoDao
import com.android.example.github.util.CoroutineTestBase
import com.android.example.github.util.InstantAppExecutors
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.RepoSearchResult
import com.android.example.github.vo.Resource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class RepoRepositoryTest : CoroutineTestBase() {
    private lateinit var repository: RepoRepository
    private lateinit var dao: RepoDao
    private val service = FakeGithubService()
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun begin() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        val db = Room.inMemoryDatabaseBuilder(app, GithubDb::class.java)
            .allowMainThreadQueries()
            .also {
                testExecutors.setupRoom(it)
            }
            .build()
        dao = db.repoDao()
        repository = RepoRepository(InstantAppExecutors(), db, dao, service)
    }

    @Test
    fun loadRepoFromNetwork() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        service.getRepoImpl = { owner, name ->
            check(owner == "foo")
            check(name == "bar")
            ApiResponse.create(Response.success(repo))
        }

        val data = repository.loadRepo("foo", "bar")
        runBlocking {
            data.addObserver().apply {
                triggerAllActions()
                assertItems(
                    Resource.loading(null),
                    Resource.success(repo)
                )
            }
        }
        // check data is inserted into the database
        dao.load("foo", "bar").addObserver().apply {
            assertItems(repo)
        }
    }

    @Test
    fun loadContributors() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val contributor = TestUtil.createContributor(repo, "log", 3)
        // network does not send these
        val contributors = listOf(contributor)
        val calledService = CompletableDeferred<Unit>()
        service.getContributorsImpl = { owner, name ->
            check(owner == "foo")
            check(name == "bar")
            calledService.complete(Unit)
            ApiResponse.create(Response.success(contributors))
        }
        val data = repository.loadContributors(
            "foo",
            "bar"
        )
        data.addObserver().apply {
            assertItems(
                Resource.loading(null),
                Resource.success(contributors)
            )
        }
        // validate that data is in database
        dao.loadContributors("foo", "bar").addObserver().apply {
            assertItems(contributors)
        }
    }

    @Test
    fun searchNextPage_null() {
        repository.searchNextPage("foo").addObserver().apply {
            assertItems(null)
        }
    }

    @Test
    fun search_fromDb() {
        val ids = arrayListOf(1, 2)

        val dbResult = RepoSearchResult("foo", ids, 2, null)
        dao.insert(dbResult)
        val repoList = ids.map { id ->
            TestUtil.createRepo(1, "owner $id", "name $id", "desc $id").also {
                runBlocking {
                    dao.insert(it)
                }
            }
        }
        repository.search("foo").addObserver().apply {
            assertItems(
                Resource.loading(null),
                Resource.success(repoList)
            )
        }
    }

    @Test
    fun search_fromServer() {
        val repo1 = TestUtil.createRepo(1, "owner", "repo 1", "desc 1")
        val repo2 = TestUtil.createRepo(2, "owner", "repo 2", "desc 2")
        service.searchReposImpl = { query ->
            check(query == "foo")
            val repoList = arrayListOf(repo1, repo2)
            ApiResponse.create(
                Response.success(RepoSearchResponse(2, repoList))
            )
        }
        repository.search("foo").addObserver().apply {
            assertItems(
                Resource.loading(null),
                Resource.success(listOf(repo1, repo2))
            )
        }
        dao.loadRepositories("owner").addObserver().apply {
            assertItems(listOf(repo1, repo2))
        }
    }

    @Test
    fun search_fromServer_error() {
        service.searchReposImpl = { query ->
            ApiResponse.create(IOException("idk"))
        }
        repository.search("foo").addObserver().apply {
            assertItems(
                Resource.loading(null),
                Resource.error("idk", null)
            )
        }
    }
}