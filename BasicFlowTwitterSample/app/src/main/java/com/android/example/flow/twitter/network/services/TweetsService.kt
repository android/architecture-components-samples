package com.android.example.flow.twitter.network.services

import com.android.example.flow.twitter.data.models.Tweet
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.QueryMap


/**
 * Created by Santanu 😁 on 2019-11-20.
 */
interface TweetsService {
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("/1.1/statuses/user_timeline.json")
    suspend fun getTweets(@HeaderMap hMap: HashMap<String, String>, @QueryMap queryMap: LinkedHashMap<String, String>): List<Tweet>
}