package com.example.benchmark

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.android.example.paging.pagingwithnetwork.GlideApp
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.reddit.ui.PostsAdapter
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import kotlinx.android.synthetic.main.activity_benchmark.*
import java.util.concurrent.Executors

class BenchmarkActivity : AppCompatActivity() {

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
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor {
                    runOnUiThread { it.run() }
                }
                .build()

        adapter.submitList(pagedStrings)
        adapter.setNetworkState(NetworkState.LOADED)
    }
}

class MockDataSource : PageKeyedDataSource<Int, RedditPost>() {
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, RedditPost>) {
        callback.onResult((0..5).map { generatePost() }.toList(), 1, 2)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, RedditPost>) {
        callback.onResult((0..5).map { generatePost() }.toList(), params.key + 1)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, RedditPost>) {
        callback.onResult((0..5).map { generatePost() }.toList(), params.key - 1)
    }

    private fun generatePost(): RedditPost {
        val title = (0..10).map { (0..100).random() }.joinToString("")
        return RedditPost("name", title, 1, "author", "androiddev", 0, System.currentTimeMillis(), null, null)
    }
}