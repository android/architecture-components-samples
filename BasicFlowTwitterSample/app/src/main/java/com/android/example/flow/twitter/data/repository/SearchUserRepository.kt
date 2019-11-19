package com.android.example.flow.twitter.data.repository

import com.android.example.flow.twitter.Constants
import com.android.example.flow.twitter.data.models.ResponseWrapper
import com.android.example.flow.twitter.data.models.User
import com.android.example.flow.twitter.hmac_sha1.OAuth1aParameters
import com.android.example.flow.twitter.network.RetrofitProvider
import com.android.example.flow.twitter.network.services.UserSearchService
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterAuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * Created by Santanu 😁 on 2019-11-17.
 */
class SearchUserRepository {

    /**
     * Member object for userSearchService
     */
    private val _userSearchService =
        RetrofitProvider.getInstance().create(UserSearchService::class.java)

    /**
     * This is a flow using suspend
     * @param query is the twitter alias
     */
    fun getFlowListUser(query: String): Flow<ResponseWrapper<out List<User>?, out Exception?>> =
        flow {
            try {
                // generating the parameters of the API
                val hMap = HashMap<String, String>()

                // the authorization parameter is generated here
                hMap["authorization"] = OAuth1aParameters(
                    TwitterAuthConfig(Constants.consumerKey, Constants.consumerSecret),
                    TwitterAuthToken(Constants.accessToken, Constants.authTokenSecret), "", "GET",
                    Constants.URL, hashMapOf("q" to query)
                ).authorizationHeader

                // count is set to 10 (max of 10 users will be extracted)
                hMap["count"] = 10.toString()

                // emit the data
                emit(ResponseWrapper(_userSearchService.getUsers(hMap, query), null))
            } catch (e: Exception) {
                emit(ResponseWrapper(null, e))
            }
        }
}