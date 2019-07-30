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

package com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byPage

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedSource
import com.android.example.paging.pagingwithnetwork.reddit.api.RedditApi
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import java.io.IOException
import java.util.concurrent.Executor

/**
 * A data source that uses the before/after keys returned in page requests.
 * <p>
 * See ItemKeyedSubredditDataSource
 */
class PageKeyedSubredditDataSource(
        private val redditApi: RedditApi,
        private val subredditName: String,
        private val retryExecutor: Executor
) : PagedSource<String, RedditPost>() {
    override val keyProvider = KeyProvider.PageKey<String, RedditPost>()

    override fun isRetryableError(error: Throwable) = false

    override suspend fun load(params: LoadParams<String>) = when (params.loadType) {
        LoadType.INITIAL -> loadInitial(params)
        LoadType.START -> loadBefore()
        LoadType.END -> loadAfter(params)
    }

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    private fun loadBefore(): LoadResult<String, RedditPost> {
        // ignored, since we only ever append to our initial load
        throw NotImplementedError()
    }

    private suspend fun loadAfter(params: LoadParams<String>): LoadResult<String, RedditPost> {
        try {
            networkState.postValue(NetworkState.LOADING)
            val result = redditApi.getTopAfter(
                    subreddit = subredditName,
                    after = params.key!!,
                    limit = params.loadSize
            )
            val data = result.data
            val items = data.children.map { it.data }
            networkState.postValue(NetworkState.LOADED)
            return LoadResult(data = items, offset = 0)
        } catch (e: IOException) {
            networkState.postValue(NetworkState.error(e.message ?: "unknown err"))
            throw e
        }
    }

    private suspend fun loadInitial(params: LoadParams<String>): LoadResult<String, RedditPost> {
        // triggered by a refresh, we better execute sync
        try {
            networkState.postValue(NetworkState.LOADING)
            initialLoad.postValue(NetworkState.LOADING)

            val result = redditApi.getTop(
                    subreddit = subredditName,
                    limit = params.loadSize
            )

            val data = result.data
            val items = data.children.map { it.data }
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            return LoadResult(
                    data = items,
                    nextKey = data.after,
                    prevKey = data.before,
                    offset = 0
            )
        } catch (ioException: IOException) {
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
            throw ioException
        }
    }
}