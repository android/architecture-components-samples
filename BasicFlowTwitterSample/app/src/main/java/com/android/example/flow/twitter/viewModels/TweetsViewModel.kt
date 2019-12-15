package com.android.example.flow.twitter.viewModels

import androidx.lifecycle.*
import com.android.example.flow.twitter.data.models.ScreenNameCount
import com.android.example.flow.twitter.data.repository.TweetsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi


/**
 * Created by Santanu 😁 on 2019-11-20.
 */
class TweetsViewModel @ExperimentalCoroutinesApi constructor(private val _tweetsRepository: TweetsRepository) : ViewModel() {

    private val _screenNameCountMutableLiveData = MutableLiveData<ScreenNameCount>()
    @ExperimentalCoroutinesApi
    val tweetsLiveData = liveData {
        emitSource(_screenNameCountMutableLiveData.switchMap { screenNameCount ->
            _tweetsRepository.getTweets(
                screenNameCount.screenName,
                screenNameCount.count
            ).asLiveData()
        })
    }

    fun setScreenNameCount(screenNameCount: ScreenNameCount) {
        _screenNameCountMutableLiveData.value = screenNameCount
    }
}