/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.samples.donuttracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.android.samples.donuttracker.storage.DonutDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DonutEntryViewModel(private val donutDao: DonutDao) : ViewModel() {

    private var donutLiveData: LiveData<Donut>? = null

    fun get(id: Long): LiveData<Donut> {
        return donutLiveData ?: liveData {
            emit(donutDao.get(id))
        }.also {
            donutLiveData = it
        }
    }

    fun addData(
        id: Long,
        name: String,
        description: String,
        rating: Int,
        setupNotification: (Long) -> Unit
    ) {
        val donut = Donut(id, name, description, rating)

        CoroutineScope(Dispatchers.Main.immediate).launch {
            var actualId = id

            if (id > 0) {
                update(donut)
            } else {
                actualId = insert(donut)
            }

            setupNotification(actualId)
        }
    }

    private suspend fun insert(donut: Donut): Long {
        return donutDao.insert(donut)
    }

    private fun update(donut: Donut) = viewModelScope.launch(Dispatchers.IO) {
        donutDao.update(donut)
    }
}
