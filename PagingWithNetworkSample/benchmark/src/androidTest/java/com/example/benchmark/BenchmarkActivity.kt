/*
 * Copyright 2019 The Android Open Source Project
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
package com.example.benchmark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.android.example.paging.pagingwithnetwork.GlideApp
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.reddit.ui.PostsAdapter
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import kotlinx.android.synthetic.main.activity_benchmark.*

class BenchmarkActivity : AppCompatActivity() {
    val testExecutor = TestExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benchmark)

        val glide = GlideApp.with(this)
        val adapter = PostsAdapter(glide) {}
        list.adapter = adapter

        val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(5)
                .setPageSize(5)
                .build()

        val pagedStrings: PagedList<RedditPost> = PagedList.Builder<Int, RedditPost>(MockDataSource(), config)
                .setInitialKey(0)
                .setFetchExecutor(testExecutor)
                .setNotifyExecutor(testExecutor)
                .build()

        adapter.submitList(pagedStrings)
        adapter.setNetworkState(NetworkState.LOADED)
    }
}

class MockDataSource : PageKeyedDataSource<Int, RedditPost>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, RedditPost>) {
        callback.onResult(List(200) { generatePost() }.toList(), -1, 1)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, RedditPost>) {
        callback.onResult(List(200) { generatePost() }.toList(), params.key + 1)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, RedditPost>) {
        callback.onResult(List(200) { generatePost() }.toList(), params.key - 1)
    }

    private fun generatePost(): RedditPost {
        val title = List(10) { (0..100).random() }.joinToString("")
        return RedditPost("name", title, 1, "author", "androiddev", 0, System.currentTimeMillis(), null, null)
    }
}