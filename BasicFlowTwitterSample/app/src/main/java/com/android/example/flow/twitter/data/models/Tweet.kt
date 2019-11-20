package com.android.example.flow.twitter.data.models

import com.google.gson.annotations.SerializedName


/**
 * Created by Santanu 😁 on 2019-11-20.
 */
class Tweet(
    @SerializedName("user") val user: User,
    @SerializedName("text") val text: String,
    @SerializedName("reply_count") val replyCount: Int,
    @SerializedName("favorite_count") val favoriteCount: Int
)