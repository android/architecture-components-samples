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

package com.android.example.paging.pagingwithnetwork.reddit.api

import android.util.Log
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API communication setup
 */
interface RedditApi {
    @GET("/r/{subreddit}/hot.json")
    fun getTop(
            @Path("subreddit") subreddit: String,
            @Query("limit") limit: Int): Call<ListingResponse>

    // for after/before param, either get from RedditDataResponse.after/before,
    // or pass RedditNewsDataResponse.name (though this is technically incorrect)
    @GET("/r/{subreddit}/hot.json")
    fun getTopAfter(
            @Path("subreddit") subreddit: String,
            @Query("after") after: String,
            @Query("limit") limit: Int): Call<ListingResponse>

    @GET("/r/{subreddit}/hot.json")
    fun getTopBefore(
            @Path("subreddit") subreddit: String,
            @Query("before") before: String,
            @Query("limit") limit: Int): Call<ListingResponse>

    class ListingResponse(val data: ListingData)

    class ListingData(
            val children: List<RedditChildrenResponse>,
            val after: String?,
            val before: String?
    )

    data class RedditChildrenResponse(val data: RedditPost)

    companion object {
        private const val BASE_URL = "https://www.reddit.com/"
        fun create(): RedditApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): RedditApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(RedditApi::class.java)
        }
    }
}