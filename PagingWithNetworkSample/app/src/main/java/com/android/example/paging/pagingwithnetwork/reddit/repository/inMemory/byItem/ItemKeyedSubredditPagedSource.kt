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

package com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byItem

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageLoadType
import androidx.paging.PagedSource
import com.android.example.paging.pagingwithnetwork.reddit.api.RedditApi
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import java.io.IOException

/**
 * A [PagedSource] that uses the "name" field of posts as the key for next/prev pages.
 *
 * Note that this is not the correct consumption of the Reddit API but rather shown here as an
 * alternative implementation which might be more suitable for your backend.
 *
 * @see com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byPage.PageKeyedSubredditPagedSource
 */
class ItemKeyedSubredditPagedSource(
        private val redditApi: RedditApi,
        private val subredditName: String
) : PagedSource<String, RedditPost>() {
    override val keyProvider = object : KeyProvider.ItemKey<String, RedditPost>() {
        /**
         * The name field is a unique identifier for post items.
         * (no it is not the title of the post :) )
         * https://www.reddit.com/dev/api
         */
        override fun getKey(item: RedditPost) = item.name
    }

    override fun isRetryableError(error: Throwable) = true

    override suspend fun load(params: LoadParams<String>) = when (params.loadType) {
        PageLoadType.REFRESH -> loadInitial(params)
        PageLoadType.START -> loadBefore()
        PageLoadType.END -> loadAfter(params)
    }

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter and we don't support loadBefore
     * in this example.
     *
     * See BoundaryCallback example for a more complete example on syncing multiple network states.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    private fun loadBefore(): LoadResult<String, RedditPost> {
        // ignored, since we only ever append to our initial load
        throw NotImplementedError()
    }

    private suspend fun loadAfter(params: LoadParams<String>): LoadResult<String, RedditPost> {
        try {
            // set network value to loading.
            networkState.postValue(NetworkState.LOADING)

            val response = redditApi.getTopAfter(
                    subreddit = subredditName,
                    after = params.key!!,
                    limit = params.loadSize
            )
            val data = response.data.children.map { it.data }
            networkState.postValue(NetworkState.LOADED)

            return LoadResult(data = data)
        } catch (e: IOException) {
            // publish the error
            networkState.postValue(NetworkState.error(e.message ?: "unknown err"))
            throw e
        }
    }

    private suspend fun loadInitial(params: LoadParams<String>): LoadResult<String, RedditPost> {
        // triggered by a refresh, we better execute sync
        try {
            // update network states.
            // we also provide an initial load state to the listeners so that the UI can know when
            // the very first list is loaded.
            networkState.postValue(NetworkState.LOADING)
            initialLoad.postValue(NetworkState.LOADING)

            val response = redditApi.getTop(subreddit = subredditName, limit = params.loadSize)
            val items = response.data.children.map { it.data }
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)

            return LoadResult(data = items)
        } catch (ioException: IOException) {
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)

            throw ioException
        }
    }
}