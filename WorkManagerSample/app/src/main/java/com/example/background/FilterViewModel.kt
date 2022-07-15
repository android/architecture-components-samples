/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * A [ViewModel] for [FilterActivity].
 *
 * Keeps track of pending image filter operations.
 */
@HiltViewModel
class FilterViewModel @Inject constructor(
    private val workManager: WorkManager
) : ViewModel() {

    //private val workManager = WorkManager.getInstance(application)

    internal val workInfo =
        workManager.getWorkInfosByTagLiveData(Constants.TAG_OUTPUT)

    internal fun apply(imageOperations: ImageOperations) {
        imageOperations.continuation.enqueue()
    }

    internal fun cancel() {
        workManager.cancelUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME)
    }
}
/*
class FilterViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FilterViewModel::class.java)) {
            FilterViewModel(application) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}*/