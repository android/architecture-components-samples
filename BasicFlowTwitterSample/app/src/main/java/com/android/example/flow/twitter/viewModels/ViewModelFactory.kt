package com.android.example.flow.twitter.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.flow.twitter.data.preferences.PreferencesManager
import com.android.example.flow.twitter.data.repository.SearchUserRepository
import com.android.example.flow.twitter.data.repository.TweetsRepository
import com.android.example.flow.twitter.network.RetrofitProvider
import com.android.example.flow.twitter.network.services.UserSearchService
import com.twitter.sdk.android.core.TwitterCore
import kotlinx.coroutines.ExperimentalCoroutinesApi


/**
 * Created by Santanu 😁 on 2019-11-19.
 */
@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val preferencesManager: PreferencesManager
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(UserSearchViewModel::class.java) ->
                    UserSearchViewModel(
                        preferencesManager,
                        SearchUserRepository(RetrofitProvider.getInstance().create(UserSearchService::class.java))
                    )
                isAssignableFrom(TweetsViewModel::class.java) ->
                    TweetsViewModel(
                        TweetsRepository()
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}