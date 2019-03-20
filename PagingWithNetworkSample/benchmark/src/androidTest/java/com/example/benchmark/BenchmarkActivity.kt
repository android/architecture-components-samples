package com.example.benchmark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.example.paging.pagingwithnetwork.GlideApp
import com.android.example.paging.pagingwithnetwork.reddit.ServiceLocator
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import com.android.example.paging.pagingwithnetwork.reddit.ui.PostsAdapter
import com.android.example.paging.pagingwithnetwork.reddit.ui.SubRedditViewModel
import kotlinx.android.synthetic.main.activity_benchmark.*

class BenchmarkActivity : AppCompatActivity() {

    private lateinit var model: SubRedditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benchmark)
        model = getViewModel()

        val glide = GlideApp.with(this)
        val adapter = PostsAdapter(glide) {
            model.retry()
        }
        list.adapter = adapter
        model.posts.observe(this, Observer {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })

        model.showSubreddit("androiddev")
    }

    private fun getViewModel(): SubRedditViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repoType = RedditPostRepository.Type.DB
                val repo = ServiceLocator.instance(this@BenchmarkActivity)
                        .getRepository(repoType)
                @Suppress("UNCHECKED_CAST")
                return SubRedditViewModel(repo) as T
            }
        })[SubRedditViewModel::class.java]
    }
}
