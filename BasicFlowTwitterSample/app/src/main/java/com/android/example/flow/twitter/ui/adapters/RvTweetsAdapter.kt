package com.android.example.flow.twitter.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.example.flow.twitter.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.twitter.sdk.android.core.models.Tweet


/**
 * Created by Santanu 😁 on 2019-11-20.
 */
class RvTweetsAdapter(private val _activity: Activity) :
    RecyclerView.Adapter<RvTweetsAdapter.TweetsViewHolder>() {
    private val _tweetsList: MutableList<Tweet> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetsViewHolder =
        TweetsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tweets_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int =
        _tweetsList.size

    override fun onBindViewHolder(holder: TweetsViewHolder, position: Int) {
        holder.bind(_tweetsList[position])
    }

    /**
     * @param apiList is the list of users received from the API
     */
    fun updateTweetsList(apiList: List<Tweet>) {
        _tweetsList.apply {
            clear()
            addAll(apiList)
            notifyDataSetChanged()
        }
    }

    inner class TweetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val _ivUser = itemView.findViewById<View>(R.id.ivUser) as ImageView
        private val _tvUserName = itemView.findViewById<View>(R.id.tvUserName) as TextView
        private val _tvTwitterHandle = itemView.findViewById<View>(R.id.tvTwitterHandle) as TextView
        private val _tvTweet = itemView.findViewById<View>(R.id.tvTweet) as TextView


        fun bind(tweet: Tweet) {
            _tvUserName.text = tweet.user.name + "(${tweet.user.name})"
            _tvTwitterHandle.text = "@${tweet.user.screenName}"
            _tvTweet.text = tweet.text

            val options = RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
            Glide.with(_activity)
                .load(tweet.user.profileImageUrlHttps)
                .apply(options)
                .into(_ivUser)
        }
    }
}