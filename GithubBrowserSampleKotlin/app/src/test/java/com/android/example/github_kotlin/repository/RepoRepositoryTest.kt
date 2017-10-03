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

@file:Suppress("FunctionName")

package com.android.example.github_kotlin.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.paging.LivePagedListProvider
import android.arch.paging.PagedList
import com.android.example.github_kotlin.api.ApiResponse
import com.android.example.github_kotlin.api.GithubService
import com.android.example.github_kotlin.api.RepoSearchResponse
import com.android.example.github_kotlin.db.GithubDb
import com.android.example.github_kotlin.db.RepoDao
import com.android.example.github_kotlin.util.*
import com.android.example.github_kotlin.vo.*
import com.nhaarman.mockito_kotlin.*
import okhttp3.Protocol
import okhttp3.Request
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import retrofit2.Response
import java.io.IOException
import java.util.*
import java.util.Collections.emptyList

@RunWith(JUnit4::class)
class RepoRepositoryTest {
    private lateinit var repository: RepoRepository
    private lateinit var dao: RepoDao
    private lateinit var service: GithubService

    @Suppress("unused")
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        dao = mock()
        service = mock()
        val db: GithubDb = mock()
        whenever(db.repoDao()).thenReturn(dao)
        repository = RepoRepository(InstantAppExecutors(), db, dao, service)
    }

    @Test
    @Throws(IOException::class)
    fun loadRepoFromNetwork() {
        val dbData = MutableLiveData<Repo>()
        whenever(dao.load("foo", "bar")).thenReturn(dbData)

        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val call = successCall(repo)
        whenever(service.getRepo("foo", "bar")).thenReturn(call)

        val data = repository.loadRepo("foo", "bar")
        verify(dao).load("foo", "bar")
        verifyNoMoreInteractions(service)

        val observer: Observer<Resource<Repo>> = mock()
        data.observeForever(observer)
        verifyNoMoreInteractions(service)
        verify(observer).onChanged(Resource.loading(null))

        val updatedDbData = MutableLiveData<Repo>()
        whenever(dao.load("foo", "bar")).thenReturn(updatedDbData)

        dbData.postValue(null)
        verify(service).getRepo("foo", "bar")
        verify(dao).insert(repo)

        updatedDbData.postValue(repo)
        verify(observer).onChanged(Resource.success(repo))
    }

    @Test
    @Throws(IOException::class)
    fun loadContributors() {
        val provider: LivePagedListProvider<Int, Contributor> = mock()

        val dbData = MutableLiveData<PagedList<Contributor>>()
        whenever(provider.create(ArgumentMatchers.isNull(), anyInt())).thenReturn(dbData)
        whenever(dao.loadContributors("foo", "bar")).thenReturn(provider)

        val data = repository.loadContributors("foo", "bar")
        verify(dao).loadContributors("foo", "bar")

        verify(service, never()).getContributors(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())

        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val contributor = TestUtil.createContributor(repo, "log", 3)
        // network does not send these
        contributor.repoOwner = ""
        contributor.repoName = ""
        val contributors = listOf(contributor)
        val call = successCall(contributors)
        whenever(service.getContributors("foo", "bar"))
                .thenReturn(call)

        val observer: Observer<Resource<PagedList<Contributor>>> = mock()
        data.observeForever(observer)

        verify(observer).onChanged(Resource.loading<PagedList<Contributor>>(null))

        dbData.value = PagedListUtil.from(emptyList())

        verify(service).getContributors("foo", "bar")
        val inserted = argumentCaptor<List<Contributor>>()
        verify(dao).insertContributors(inserted.capture())


        assertThat(inserted.lastValue.size, `is`(1))
        val first = inserted.lastValue[0]
        assertThat(first.repoName, `is`("bar"))
        assertThat(first.repoOwner, `is`("foo"))

        dbData.value = PagedListUtil.from(contributors)
        verify(observer).onChanged(Resource.success(PagedListUtil.from(contributors)))
    }

    @Test
    fun loadContributors_204_NoContent() {
        val provider: LivePagedListProvider<Int, Contributor> = mock()
        val dbData = MutableLiveData<PagedList<Contributor>>()
        whenever(provider.create(ArgumentMatchers.isNull(), anyInt())).thenReturn(dbData)
        whenever(dao.loadContributors("foo", "bar")).thenReturn(provider)

        val data = repository.loadContributors("foo", "bar")

        verify(dao).loadContributors("foo", "bar")
        verify(service, never()).getContributors(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())


        val contributors: List<Contributor>? = null

        val call = createCall(Response.success(contributors,
                                                  okhttp3.Response.Builder()
                                                          .code(204)
                                                          .message("No Content")
                                                          .protocol(Protocol.HTTP_1_1)
                                                          .request(Request.Builder().url("http://localhost/").build())
                                                          .build()))

        whenever(service.getContributors("foo", "bar"))
                .thenReturn(call)

        val observer: Observer<Resource<PagedList<Contributor>>> = mock()
        data.observeForever(observer)

        verify(observer).onChanged(Resource.loading<PagedList<Contributor>>(null))

        dbData.value = PagedListUtil.from(emptyList())

        verify(service).getContributors("foo", "bar")

        verify(dao, never()).insertContributors(any())
    }

    @Test
    fun searchNextPage_null() {
        whenever(dao.findSearchResult("foo")).thenReturn(null)
        val observer: Observer<Resource<Boolean>> = mock()
        repository.searchNextPage("foo").observeForever(observer)
        verify(observer).onChanged(null)
    }

    @Test
    fun search_fromDb() {
        val ids = Arrays.asList(1, 2)

        val observer: Observer<Resource<List<Repo>>> = mock()
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        val repositories = MutableLiveData<List<Repo>>()

        whenever(dao.search("foo")).thenReturn(dbSearchResult)

        repository.search("foo").observeForever(observer)

        verify(observer).onChanged(Resource.loading<List<Repo>>(null))
        verifyNoMoreInteractions(service)
        reset(observer)

        val dbResult = RepoSearchResult("foo", ids, 2, null)
        whenever(dao.loadOrdered(ids)).thenReturn(repositories)

        dbSearchResult.postValue(dbResult)

        val repoList = ArrayList<Repo>()
        repositories.postValue(repoList)
        verify(observer).onChanged(Resource.success<List<Repo>>(repoList))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun search_fromServer() {
        val ids = Arrays.asList(1, 2)
        val repo1 = TestUtil.createRepo(1, "owner", "repo 1", "desc 1")
        val repo2 = TestUtil.createRepo(2, "owner", "repo 2", "desc 2")

        val observer: Observer<Resource<List<Repo>>> = mock()
        val dbSearchResult = MutableLiveData<RepoSearchResult>()
        val repositories = MutableLiveData<List<Repo>>()

        val apiResponse = RepoSearchResponse()
        val repoList = Arrays.asList(repo1, repo2)
        apiResponse.items = repoList
        apiResponse.total = 2

        val callLiveData = MutableLiveData<ApiResponse<RepoSearchResponse>>()
        whenever(service.searchRepos("foo")).thenReturn(callLiveData)

        whenever(dao.search("foo")).thenReturn(dbSearchResult)

        repository.search("foo").observeForever(observer)

        verify(observer).onChanged(Resource.loading<List<Repo>>(null))
        verifyNoMoreInteractions(service)
        reset(observer)

        whenever(dao.loadOrdered(ids)).thenReturn(repositories)
        dbSearchResult.postValue(null)
        verify(dao, never()).loadOrdered(any())

        verify(service).searchRepos("foo")
        val updatedResult = MutableLiveData<RepoSearchResult>()
        whenever(dao.search("foo")).thenReturn(updatedResult)
        updatedResult.postValue(RepoSearchResult("foo", ids, 2, null))

        callLiveData.postValue(ApiResponse(Response.success(apiResponse)))
        verify(dao).insertRepos(repoList)
        repositories.postValue(repoList)
        verify(observer).onChanged(Resource.success(repoList))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun search_fromServer_error() {
        whenever(dao.search("foo")).thenReturn(AbsentLiveData.create())
        val apiResponse = MutableLiveData<ApiResponse<RepoSearchResponse>>()
        whenever(service.searchRepos("foo")).thenReturn(apiResponse)

        val observer: Observer<Resource<List<Repo>>> = mock()
        repository.search("foo").observeForever(observer)
        verify(observer).onChanged(Resource.loading<List<Repo>>(null))

        apiResponse.postValue(ApiResponse(Exception("idk")))
        verify(observer).onChanged(Resource.error<List<Repo>>("idk", null))
    }
}