package com.android.example.flow.twitter.viewModels

import androidx.lifecycle.*
import com.android.example.flow.twitter.data.models.ResponseWrapper
import com.android.example.flow.twitter.data.models.User
import com.android.example.flow.twitter.data.repository.SearchUserRepository


/**
 * Created by Santanu 😁 on 2019-11-17.
 */
class UserSearchViewModel : ViewModel() {

    /**
     * Member variable for repository
     */
    private val _searchUserRepository = SearchUserRepository()

    /**
     * When this mutable LiveData is observed internally by the LiveData observed in the activity
     */
    private val _searchQueryMutableLiveData = MutableLiveData<String>()

    /**
     * LiveData observed in the Activity
     */
    val twitteeLiveData: LiveData<ResponseWrapper<out List<User>?, out Exception?>> = liveData {
        emitSource(_searchQueryMutableLiveData.switchMap {
            _searchUserRepository.getFlowListUser(it).asLiveData()
        })
    }

    /**
     * LiveData object that collects the flow
     * @param query is the twitter alias for which the user is searching
     */
    fun getUsers(query: String) {
        _searchQueryMutableLiveData.value = query
    }
}