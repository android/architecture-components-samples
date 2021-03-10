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
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.example.paging.pagingwithnetwork.GlideApp
import com.android.example.paging.pagingwithnetwork.reddit.ui.PostsAdapter
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import com.example.benchmark.databinding.ActivityBenchmarkBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BenchmarkActivity : AppCompatActivity() {
    val testExecutor = TestExecutor()
    @VisibleForTesting
    lateinit var binding: ActivityBenchmarkBinding
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBenchmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val glide = GlideApp.with(this)
        val adapter = PostsAdapter(glide)
        binding.list.adapter = adapter

        val config = PagingConfig(
            pageSize = 5,
            initialLoadSize = 5
        )

        val pager = Pager(config, 0) {
            MockPagingSource()
        }

        lifecycleScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            pager.flow.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}

class MockPagingSource : PagingSource<Int, RedditPost>() {
    private fun generatePost(): RedditPost {
        val title = List(10) { (0..100).random() }.joinToString("")
        return RedditPost("name", title, 1, "author", "androiddev", 0, System.currentTimeMillis(), null, null)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RedditPost> {
        val key = params.key ?: 0
        return LoadResult.Page(List(200) { generatePost() }.toList(), key - 1, key + 1)
    }

    // Unused in benchmark.
    override fun getRefreshKey(state: PagingState<Int, RedditPost>): Int? = null
}