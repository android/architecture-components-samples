package com.android.example.flow.twitter.data.models

import com.google.gson.annotations.SerializedName


/**
 * Created by Santanu 😁 on 2019-11-17.
 */
class User(
    @SerializedName("name") var name: String = "",
    @SerializedName("screen_name") var screenName: String = "",
    @SerializedName("followers_count") var followersCount: Long,
    @SerializedName("profile_banner_url") var profileBanner: String,
    @SerializedName("profile_image_url_https") var profileImage: String
)