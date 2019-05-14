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
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.android.example.github.api.ApiResponse
import com.android.example.github.api.FakeGithubService
import com.android.example.github.api.RepoSearchResponse
import com.android.example.github.db.GithubDb
import com.android.example.github.db.RepoDao
import com.android.example.github.util.CoroutineTestBase
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.RepoSearchResult
import com.android.example.github.vo.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class FetchNextSearchPageTaskTest : CoroutineTestBase() {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val service = FakeGithubService()

    private lateinit var db: GithubDb

    private lateinit var repoDao: RepoDao

    private lateinit var task: LiveData<Resource<Boolean>?>

    @Before
    fun createSubjects() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, GithubDb::class.java)
            .allowMainThreadQueries()
            .also {
                testExecutors.setupRoom(it)
            }
            .build()
        repoDao = db.repoDao()
        task = fetchNextSearch("foo", service, db, testExecutors.ioDispatcher)
    }

    @Test
    fun withoutResult() {
        task.addObserver().apply {
            assertItems(null)
        }
    }

    @Test
    fun noNextPage() {
        createDbResult(null)
        task.addObserver().apply {
            assertItems(Resource.success(false))
        }
    }

    @Test
    fun nextPageWithNull() {
        createDbResult(1)
        val repos = TestUtil.createRepos(10, "a", "b", "c")
        val result = RepoSearchResponse(10, repos)
        createCall(result, null)
        task.addObserver().apply {
            assertItems(Resource.success(false))
        }
        // ensure they are in the database
        repoDao.loadOrdered(repos.map { it.id }).addObserver().apply {
            assertItems(repos)
        }
    }

    @Test
    fun nextPageWithMore() {
        createDbResult(1)
        val repos = TestUtil.createRepos(10, "a", "b", "c")
        val result = RepoSearchResponse(10, repos)
        result.nextPage = 2
        createCall(result, 2)
        task.addObserver().apply {
            assertItems(Resource.success(true))
        }
        // ensure they are in the database
        repoDao.loadOrdered(repos.map { it.id }).addObserver().apply {
            assertItems(repos)
        }
    }

    @Test
    fun nextPageApiError() {
        createDbResult(1)
        service.searchReposPagedImpl = { query, page ->
            check(query == "foo")
            check(page == 1)
            ApiResponse.create(
                Response.error(
                    400, ResponseBody.create(
                        MediaType.parse("txt"), "bar"
                    )
                )
            )
        }
        task.addObserver().apply {
            assertItems(Resource.error("bar", true))
        }
    }

    @Test
    fun nextPageIOError() {
        createDbResult(1)
        service.searchReposPagedImpl = { query, page ->
            check(query == "foo")
            check(page == 1)
            throw IOException("bar")
        }
        task.addObserver().apply {
            assertItems(Resource.error("bar", true))
        }
    }

    private fun createDbResult(nextPage: Int?) {
        val result = RepoSearchResult(
            "foo", emptyList(),
            0, nextPage
        )
        repoDao.insert(result)
    }

    private fun createCall(body: RepoSearchResponse, nextPage: Int?) {
        val headers = if (nextPage == null)
            null
        else
            Headers
                .of(
                    "link",
                    "<https://api.github.com/search/repositories?q=foo&page=" + nextPage
                            + ">; rel=\"next\""
                )
        val response = if (headers == null)
            Response.success(body)
        else
            Response.success(body, headers)
        service.searchReposPagedImpl = { query, page ->
            check(query == "foo") {
                "query should be foo but it was $query"
            }
            val expectedPageArg = nextPage?.let { it - 1 } ?: 1
            check(page == expectedPageArg) {
                "next page should be $expectedPageArg but it was $page"
            }
            ApiResponse.create(response)
        }
    }
}