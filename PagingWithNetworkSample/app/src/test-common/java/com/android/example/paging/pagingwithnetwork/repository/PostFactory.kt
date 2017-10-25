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

import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import java.util.concurrent.atomic.AtomicInteger

class PostFactory {
    private val counter = AtomicInteger(0)
    fun createRedditPost(subredditName : String) : RedditPost {
        val id = counter.incrementAndGet()
        val post = RedditPost(
                name = "name_$id",
                title = "title $id",
                score = 10,
                author = "author $id",
                num_comments = 0,
                created = System.currentTimeMillis(),
                thumbnail = null,
                subreddit = subredditName,
                url = null
        )
        post.indexInResponse = -1
        return post
    }
}