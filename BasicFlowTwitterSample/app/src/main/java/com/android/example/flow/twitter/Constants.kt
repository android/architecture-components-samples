package com.android.example.flow.twitter

import android.util.Base64
import java.util.concurrent.TimeUnit


/**
 * Created by Santanu 😁 on 2019-11-17.
 */
class Constants {
    companion object {
        const val SCREEN_NAME = "screenName"
        const val QUERIES: String = "queriesSet"
        const val PREFS: String = "twitterPrefs"
        val consumerKey = "nDlWnChz9pVSZBgLsL6JTuEkz"
        val accessToken = "1195684525637955584-qBIvzaRjwaxhZUhwh8De1NU9Vv1kDR"
        val consumerSecret = "EU640BporNu4NaZWInU28vsq5fpcuxlTw3iKtX7i7lEPJtWW3f"
        val authTokenSecret = "ivp6qmKDtjB7050JEzJLY8COKKD9He8fKJPfPu78JkD2H"

        val TwitterHeader = "OAuth " +
                "oauth_consumer_key=\"$consumerKey\"" + ", " +
                "oauth_nonce=\"{nonce}\"" + ", " +
                "oauth_signature=\"{signature}\"" + ", " +
                "oauth_signature_method=\"HMAC-SHA1\"" + ", " +
                "oauth_timestamp=\"{timestamp}\"" + ", " +
                "oauth_token=\"$accessToken\"" + ", " +
                "oauth_version=\"1.0\""

        const val TwitterParams = "oauth_consumer_key=nDlWnChz9pVSZBgLsL6JTuEkz" + "&" +
                "oauth_nonce={nonce}" + "&" +
                "oauth_signature_method=HMAC-SHA1" + "&" +
                "oauth_timestamp={timestamp}" + "&" +
                "oauth_token=1195684525637955584-qBIvzaRjwaxhZUhwh8De1NU9Vv1kDR" + "&" +
                "oauth_version=1.0"

        val URL = "https://api.twitter.com/1.1/users/search.json"

        fun generateNonce(timeStamp: String): String {
            return String(
                Base64.encode(
                    ("nDlWnChz9pVSZBgLsL6JTuEkz:$timeStamp").toByteArray(),
                    Base64.DEFAULT
                )
            ).trim()
        }

        fun generateTimeStamp(): String =
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
    }
}