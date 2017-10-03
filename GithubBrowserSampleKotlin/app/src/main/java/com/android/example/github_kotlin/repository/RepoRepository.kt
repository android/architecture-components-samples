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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.PagedList
import com.android.example.github_kotlin.AppExecutors
import com.android.example.github_kotlin.api.ApiResponse
import com.android.example.github_kotlin.api.GithubService
import com.android.example.github_kotlin.api.RepoSearchResponse
import com.android.example.github_kotlin.db.GithubDb
import com.android.example.github_kotlin.db.RepoDao
import com.android.example.github_kotlin.util.AbsentLiveData
import com.android.example.github_kotlin.util.RateLimiter
import com.android.example.github_kotlin.vo.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles Repo instances.
 *
 * unfortunate naming :/ .
 * Repo - value object name
 * Repository - type of this class.
 */
@Singleton
class RepoRepository
@Inject
constructor(private val appExecutors: AppExecutors,
            private val db: GithubDb,
            private val repoDao: RepoDao,
            private val githubService: GithubService) {

    private val repoListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadRepos(owner: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, List<Repo>>(appExecutors) {
            override fun saveCallResult(item: List<Repo>) {
                repoDao.insertRepos(item)
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || data.isEmpty() || repoListRateLimit.shouldFetch(owner)
            }

            override fun loadFromDb(): LiveData<List<Repo>> {
                return repoDao.loadRepositories(owner)
            }

            override fun createCall(): LiveData<ApiResponse<List<Repo>>> {
                return githubService.getRepos(owner)
            }

            override fun onFetchFailed() {
                repoListRateLimit.reset(owner)
            }
        }.asLiveData()
    }

    fun loadRepo(owner: String?, name: String?): LiveData<Resource<Repo>> {
        return object : NetworkBoundResource<Repo, Repo>(appExecutors) {
            override fun saveCallResult(item: Repo) {
                repoDao.insert(item)
            }

            override fun shouldFetch(data: Repo?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<Repo> {
                return repoDao.load(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<Repo>> {
                return githubService.getRepo(owner!!, name!!)
            }
        }.asLiveData()
    }

    fun loadContributors(owner: String, name: String): LiveData<Resource<PagedList<Contributor>>> {
        return object : NetworkBoundResource<PagedList<Contributor>, List<Contributor>>(appExecutors) {
            override fun saveCallResult(item: List<Contributor>) {
                for (contributor in item) {
                    contributor.repoName = name
                    contributor.repoOwner = owner
                }
                db.beginTransaction()
                try {
                    repoDao.createRepoIfNotExists(Repo(Repo.UNKNOWN_ID,
                                                       name, "$owner/$name", "",
                                                       Repo.Owner(owner, null), 0))
                    repoDao.insertContributors(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                Timber.d("rece saved item to db")
            }

            override fun shouldFetch(data: PagedList<Contributor>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<PagedList<Contributor>> {
                val provider = repoDao.loadContributors(
                        owner, name)
                return provider.create(null, 50)
            }

            override fun createCall(): LiveData<ApiResponse<List<Contributor>>> {
                return githubService.getContributors(owner, name)
            }
        }.asLiveData()
    }

    fun searchNextPage(query: String): LiveData<Resource<Boolean>> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
                query, githubService, db)
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.liveData
    }

    fun search(query: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(appExecutors) {

            override fun saveCallResult(item: RepoSearchResponse) {
                val repoIds = item.repoIds
                val repoSearchResult = RepoSearchResult(query, repoIds, item.total, item.nextPage)
                db.beginTransaction()
                try {
                    item.items?.let { repoDao.insertRepos(it) }
                    repoDao.insert(repoSearchResult)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<List<Repo>> {
                return Transformations.switchMap(repoDao.search(query)) {
                    when (it) {
                        null -> AbsentLiveData.create()
                        else -> repoDao.loadOrdered(it.repoIds)
                    }
                }
            }

            override fun createCall(): LiveData<ApiResponse<RepoSearchResponse>> {
                return githubService.searchRepos(query)
            }

            override fun processResponse(response: ApiResponse<RepoSearchResponse>?): RepoSearchResponse? {
                val body = response?.body
                if (body != null) {
                    body.nextPage = response.nextPage
                }
                return body
            }
        }.asLiveData()
    }
}
