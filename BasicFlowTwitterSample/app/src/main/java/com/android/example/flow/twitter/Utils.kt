package com.android.example.flow.twitter

import com.android.example.flow.twitter.hmac_sha1.OAuth1aParameters
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterAuthToken


/**
 * Created by Santanu 😁 on 2019-11-19.
 */
fun getTwitterHeaderMap(queryMap: HashMap<String, String>): HashMap<String, String> {
    // generating the parameters of the API
    val hMap = HashMap<String, String>()

    // the authorization parameter is generated here
    hMap["authorization"] = OAuth1aParameters(
        TwitterAuthConfig(Constants.consumerKey, Constants.consumerSecret),
        TwitterAuthToken(Constants.accessToken, Constants.authTokenSecret), "", "GET",
        Constants.URL, queryMap
    ).authorizationHeader

    return hMap
}