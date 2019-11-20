package com.android.example.flow.twitter.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelLazy
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.flow.twitter.Constants
import com.android.example.flow.twitter.data.models.ScreenNameCount
import com.android.example.flow.twitter.data.preferences.PreferencesManager
import com.android.example.flow.twitter.ui.adapters.RvTweetsAdapter
import com.android.example.flow.twitter.viewModels.TweetsViewModel
import com.android.example.flow.twitter.viewModels.ViewModelFactory
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.activity_tweets.*

class TweetsActivity : AppCompatActivity() {

    private val _viewModel: TweetsViewModel by ViewModelLazy(
        TweetsViewModel::class,
        { viewModelStore },
        { ViewModelFactory(PreferencesManager(this)) })

    private lateinit var _rvTweetsAdapter: RvTweetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.android.example.flow.twitter.R.layout.activity_tweets)

        setUpRecyclerView()
        observeTweets()

        //start the operation
        intent.getStringExtra(Constants.SCREEN_NAME)?.let {
            _viewModel.setScreenNameCount(ScreenNameCount(it, 10))
        }

        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(
                TwitterAuthConfig(
                    Constants.consumerKey,
                    Constants.consumerSecret
                )
            )
            .debug(true)
            .build()
        Twitter.initialize(config)
    }

    private fun setUpRecyclerView() {
        // users list recyclerView
        _rvTweetsAdapter = RvTweetsAdapter(this)
        rvTweets.layoutManager = LinearLayoutManager(this)
        rvTweets.adapter = _rvTweetsAdapter
    }

    private fun observeTweets() {
        _viewModel.tweetsLiveData.observe(this, Observer {
            it.exception?.let { exception ->
                handleException(exception)
            } ?: kotlin.run {
                handleData(it.data!!)
            }
        })
    }

    private fun handleData(data: List<Tweet>) {
        _rvTweetsAdapter.updateTweetsList(data)
    }

    private fun handleException(exception: Exception) {
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
    }
}
