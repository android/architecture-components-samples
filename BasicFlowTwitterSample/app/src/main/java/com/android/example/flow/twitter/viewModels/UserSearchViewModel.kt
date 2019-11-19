package com.android.example.flow.twitter.viewModels

import androidx.lifecycle.*
import com.android.example.flow.twitter.data.models.ResponseWrapper
import com.android.example.flow.twitter.data.models.User
import com.android.example.flow.twitter.data.preferences.PreferencesManager
import com.android.example.flow.twitter.data.repository.SearchUserRepository


/**
 * Created by Santanu 😁 on 2019-11-17.
 */
class UserSearchViewModel(private var preferencesManager: PreferencesManager) : ViewModel() {

    /**
     * Member variable for repository
     */
    private val _searchUserRepository = SearchUserRepository()

    /**
     * When this mutable LiveData is observed internally by the LiveData observed in the activity
     */
    private val _searchQueryMutableLiveData = MutableLiveData<String>()

    /**
     * It holds all cached queries
     */
    private val _queriesMutableLiveData = MutableLiveData<MutableSet<String>>()

    /**
     * It observe each text-change of the search View ( except empty )
     */
    private val _textChangeMutableLiveData = MutableLiveData<String>()

    init {
        _queriesMutableLiveData.value = preferencesManager.getAllQueries()
    }

    /**
     * Store queries to local
     * When textChange liveData is altered , it filters out the set containing the textChange
     * and returns a list
     */
    val textChangeLiveData: LiveData<List<String>?> = liveData {
        emitSource(_textChangeMutableLiveData.switchMap { textChange ->
            liveData {
                emit(_queriesMutableLiveData.value?.filter { query ->
                    query.contains(
                        textChange
                    )
                })
            }
        })
    }

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
        preferencesManager.storeQuery(query)
        _queriesMutableLiveData.value = preferencesManager.getAllQueries()
    }

    /**
     * @param query is the text changed query
     */
    fun setTextChange(textChange: String) {
        if (textChange.isNotEmpty())
            _textChangeMutableLiveData.value = textChange
    }
}