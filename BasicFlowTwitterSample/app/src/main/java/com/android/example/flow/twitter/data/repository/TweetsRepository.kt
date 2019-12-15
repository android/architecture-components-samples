package com.android.example.flow.twitter.data.repository

import android.util.Log
import com.android.example.flow.twitter.data.models.ResponseWrapper
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


/**
 * Created by Santanu 😁 on 2019-11-20.
 */
@ExperimentalCoroutinesApi
class TweetsRepository {

    /**
     * @param screenName  is the unique name of the user
     * @param count       is the count of tweets
     */
    fun getTweets(
        screenName: String,
        count: Int
    ): Flow<ResponseWrapper<out List<Tweet>?, out Exception?>> = callbackFlow {

        val call = TwitterCore.getInstance().apiClient.statusesService.userTimeline(
            null, screenName, count,
            null, null, null, null, null, null
        )

        val callback = object : Callback<List<Tweet>>() {
            override fun success(result: com.twitter.sdk.android.core.Result<List<Tweet>>?) {
                offer(ResponseWrapper(result?.data, null))
            }

            override fun failure(exception: TwitterException?) {
                Log.e("error", exception?.message.toString())
                offer(ResponseWrapper(null, exception))
                close()
            }

        }
        call.enqueue(callback)
        awaitClose {  }

        /*try {
            val queryMap = linkedMapOf(
                "screen_name" to screenName*//*,
                "count" to count.toString()*//*
            )
            val headerMap = getTwitterHeaderMap(queryMap)
            emit(ResponseWrapper(_tweetsService.getTweets(headerMap, queryMap), null))

            // this will refresh the tweets every 200 millis
            while (true) {
                delay(200)
                emit(ResponseWrapper(_tweetsService.getTweets(headerMap, queryMap), null))
            }
        } catch (e: Exception) {
            emit(ResponseWrapper(null, e))
        }*/

    }
}