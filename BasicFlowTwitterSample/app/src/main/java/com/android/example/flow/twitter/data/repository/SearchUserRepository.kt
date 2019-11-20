package com.android.example.flow.twitter.data.repository

import com.android.example.flow.twitter.data.models.ResponseWrapper
import com.android.example.flow.twitter.data.models.User
import com.android.example.flow.twitter.getTwitterHeaderMap
import com.android.example.flow.twitter.network.services.UserSearchService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * Created by Santanu 😁 on 2019-11-17.
 */
class SearchUserRepository(private var _userSearchService: UserSearchService) {

    /**
     * This is a flow using suspend
     * @param query is the twitter alias
     */
    fun getFlowListUser(query: String): Flow<ResponseWrapper<out List<User>?, out Exception?>> =
        flow {
            try {
                val queryMap = hashMapOf(
                    "q" to query,
                    "count" to 10.toString()
                )

                emit(
                    ResponseWrapper(
                        _userSearchService.getUsers(
                            getTwitterHeaderMap(queryMap),
                            queryMap
                        ), null
                    )
                )
            } catch (e: Exception) {
                emit(ResponseWrapper(null, e))
            }
        }
}