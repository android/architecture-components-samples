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

package com.android.example.paging.pagingwithnetwork.repository

import com.android.example.paging.pagingwithnetwork.reddit.api.RedditApi
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import kotlin.math.min

/**
 * implements the RedditApi with controllable requests
 */
class FakeRedditApi : RedditApi {
    // subreddits keyed by name
    private val model = mutableMapOf<String, SubReddit>()
    var failureMsg: String? = null
    fun addPost(post: RedditPost) {
        val subreddit = model.getOrPut(post.subreddit) {
            SubReddit(items = arrayListOf())
        }
        subreddit.items.add(post)
    }

    private fun findPosts(
            subreddit: String,
            limit: Int,
            after: String? = null
    ): List<RedditApi.RedditChildrenResponse> {
        val subReddit = findSubReddit(subreddit)
        val posts = subReddit.findPosts(limit, after)
        return posts.map { RedditApi.RedditChildrenResponse(it.copy()) }
    }

    private fun findSubReddit(subreddit: String) =
            model.getOrDefault(subreddit, SubReddit())

    override suspend fun getTop(
            @Path("subreddit") subreddit: String,
            @Query("limit") limit: Int,
            @Query("after") after: String?,
            @Query("before") before: String?
    ): RedditApi.ListingResponse {
        failureMsg?.let {
            throw IOException(it)
        }
        val items = findPosts(subreddit, limit)
        val after = items.lastOrNull()?.data?.name
        return RedditApi.ListingResponse(
                RedditApi.ListingData(
                        children = items,
                        after = after,
                        before = null
                )
        )
    }

    private class SubReddit(val items: MutableList<RedditPost> = arrayListOf()) {
        fun findPosts(limit: Int, after: String?): List<RedditPost> {
            if (after == null) {
                return items.subList(0, min(items.size, limit))
            }
            val index = items.indexOfFirst { it.name == after }
            if (index == -1) {
                return emptyList()
            }
            val startPos = index + 1
            return items.subList(startPos, min(items.size, startPos + limit))
        }
    }
}