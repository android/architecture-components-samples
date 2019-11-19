package com.android.example.flow.twitter.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.flow.twitter.data.preferences.PreferencesManager


/**
 * Created by Santanu 😁 on 2019-11-19.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(private var preferencesManager: PreferencesManager) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(UserSearchViewModel::class.java) ->
                    UserSearchViewModel(preferencesManager)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}