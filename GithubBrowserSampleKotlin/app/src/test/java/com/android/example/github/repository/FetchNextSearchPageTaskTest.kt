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

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.android.example.github.api.GithubService
import com.android.example.github.api.RepoSearchResponse
import com.android.example.github.db.GithubDb
import com.android.example.github.db.RepoDao
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.RepoSearchResult
import com.android.example.github.vo.Resource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@RunWith(JUnit4::class)
class FetchNextSearchPageTaskTest {

    @Suppress("unused")
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: GithubService

    private lateinit var db: GithubDb

    private lateinit var repoDao: RepoDao

    private lateinit var task: FetchNextSearchPageTask

    private lateinit var observer: Observer<Resource<Boolean>>

    @Before
    fun init() {
        service = mock()
        db = mock()
        repoDao = mock()
        whenever(db.repoDao()).thenReturn(repoDao)
        task = FetchNextSearchPageTask("foo", service, db)

        observer = mock()
        task.liveData.observeForever(observer)
    }

    @Test
    fun withoutResult() {
        whenever(repoDao.search("foo")).thenReturn(null)
        task.run()
        verify<Observer<Resource<Boolean>>>(observer).onChanged(null)
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun noNextPage() {
        createDbResult(null)
        task.run()
        verify<Observer<Resource<Boolean>>>(observer).onChanged(Resource.success(false))
        verifyNoMoreInteractions(observer)
        verifyNoMoreInteractions(service)
    }

    @Test
    @Throws(IOException::class)
    fun nextPageWithNull() {
        createDbResult(1)
        val result = RepoSearchResponse()
        result.total = 10
        val repos = TestUtil.createRepos(10, "a", "b", "c")
        result.items = repos
        val call = createCall(result, null)
        whenever(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify<RepoDao>(repoDao).insertRepos(repos)
        verify<Observer<Resource<Boolean>>>(observer).onChanged(Resource.success(false))
    }

    @Test
    @Throws(IOException::class)
    fun nextPageWithMore() {
        createDbResult(1)
        val result = RepoSearchResponse()
        result.total = 10
        val repos = TestUtil.createRepos(10, "a", "b", "c")
        result.items = repos
        result.nextPage = 2
        val call = createCall(result, 2)
        whenever(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify<RepoDao>(repoDao).insertRepos(repos)
        verify<Observer<Resource<Boolean>>>(observer).onChanged(Resource.success(true))
    }

    @Test
    @Throws(IOException::class)
    fun nextPageApiError() {
        createDbResult(1)
        val call: Call<RepoSearchResponse> = mock()
        whenever(call.execute()).thenReturn(Response.error(400, ResponseBody.create(MediaType.parse("txt"), "bar")))
        whenever(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify<Observer<Resource<Boolean>>>(observer).onChanged(Resource.error("bar", true))
    }

    @Test
    @Throws(IOException::class)
    fun nextPageIOError() {
        createDbResult(1)
        val call: Call<RepoSearchResponse> = mock()
        whenever(call.execute()).thenThrow(IOException("bar"))
        whenever(service.searchRepos("foo", 1)).thenReturn(call)
        task.run()
        verify<Observer<Resource<Boolean>>>(observer).onChanged(Resource.error("bar", true))
    }

    private fun createDbResult(nextPage: Int?) {
        val result = RepoSearchResult("foo", emptyList(),
                                      0, nextPage)
        whenever(repoDao.findSearchResult("foo")).thenReturn(result)
    }

    @Throws(IOException::class)
    private fun createCall(body: RepoSearchResponse, nextPage: Int?): Call<RepoSearchResponse> {
        val headers = if (nextPage == null)
            null
        else
            Headers
                    .of("link",
                        "<https://api.github.com/search/repositories?q=foo&page=" + nextPage
                                + ">; rel=\"next\"")
        val success = if (headers == null)
            Response.success(body)
        else
            Response.success(body, headers)
        val call: Call<RepoSearchResponse> = mock()
        whenever(call.execute()).thenReturn(success)

        return call
    }
}