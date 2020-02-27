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

import androidx.paging.PagingConfig
import androidx.paging.PagingDataFlow
import com.android.example.paging.pagingwithnetwork.reddit.api.RedditApi
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Repository implementation that that loads data directly from the network and uses the Item's name
 * as the key to discover prev/next pages.
 */
class InMemoryByItemRepository(
        private val redditApi: RedditApi,
        private val networkDispatcher: CoroutineDispatcher
) : RedditPostRepository {
    override fun postsOfSubreddit(subReddit: String, pageSize: Int) = PagingDataFlow(
            PagingConfig(
                    pageSize = pageSize,
                    enablePlaceholders = false,
                    initialLoadSize = pageSize * 2
            )
    ) {
        ItemKeyedSubredditPagingSource(
                redditApi = redditApi,
                subredditName = subReddit,
                networkDispatcher = networkDispatcher
        )
    }
}
