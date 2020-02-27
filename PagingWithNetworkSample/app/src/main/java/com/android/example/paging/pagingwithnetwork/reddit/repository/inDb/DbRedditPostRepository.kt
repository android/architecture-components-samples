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

package com.android.example.paging.pagingwithnetwork.reddit.repository.inDb

import androidx.paging.PagingConfig
import androidx.paging.PagingDataFlow
import com.android.example.paging.pagingwithnetwork.reddit.db.RedditDb
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 */
class DbRedditPostRepository(
        val db: RedditDb,
        private val ioDispatcher: CoroutineDispatcher
) : RedditPostRepository {
    /**
     * Returns a Listing for the given [subReddit].
     */
    override fun postsOfSubreddit(subReddit: String, pageSize: Int) = PagingDataFlow(
            config = PagingConfig(pageSize),
            pagingSourceFactory = db.posts().postsBySubreddit(subReddit)
                    .asPagingSourceFactory(ioDispatcher)
    )
}
