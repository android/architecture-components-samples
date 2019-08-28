/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.android.example.livedatabuilder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.android.example.livedatabuilder.util.MainCoroutineRule
import com.android.example.livedatabuilder.util.getOrAwaitValue
import com.android.example.livedatabuilder.util.observeForTesting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

/**
 * Unit tests for [LiveDataViewModel].
 */
@ExperimentalCoroutinesApi
class LiveDataViewModelTest {

    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    // Sets the main coroutines dispatcher to a TestCoroutineScope for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Use a Fake DataSource so we have all necessary control over it
    private val fakeDataSource = FakeDataSource()

    // Class under test. Uses Dispatchers.Main so that the MainCoroutineRule can control it.
    private val viewModel = LiveDataViewModel(fakeDataSource)

    @Test
    fun getCurrentTime_notEmpty() {
        val cachedValue = viewModel.cachedValue.getOrAwaitValue()
        assertTrue(cachedValue.isNotEmpty())
    }

    @Test
    fun currentTimeTransformed() {
        // Get the result of a coroutine inside a transformation
        val timeTransformed = viewModel.currentTimeTransformed.getOrAwaitValue {
            // After observing, advance the clock to avoid the delay calls.
            mainCoroutineRule.advanceUntilIdle()
        }
        assertEquals(timeTransformed, Date(FakeDataSource.CURRENT_TIME).toString())
    }

    @Test
    fun getCurrentWeather_loading() {
        // Start with a paused dispatcher in the FakeDataSource
        fakeDataSource.testDispatcher.pauseDispatcher()

        // Keep observing currentWeather
        viewModel.currentWeather.observeForTesting {

            // Verify that the first value is Loading
            assertEquals(viewModel.currentWeather.value, LiveDataViewModel.LOADING_STRING)

            // Resume fake dispatcher so it emits a new value
            fakeDataSource.testDispatcher.resumeDispatcher()

            // Verify the new value is available
            assertEquals(viewModel.currentWeather.value, FakeDataSource.WEATHER_CONDITION)
        }
    }

    @Test
    fun cache_RefreshFromViewModelScope() {
        // Get the initial value that comes directly from FakeDataSource
        val initialValue = viewModel.cachedValue.getOrAwaitValue()

        // Trigger an update, which starts a coroutine that updates the value
        viewModel.onRefresh()

        // Get the new value
        val valueAfterRefresh = viewModel.cachedValue.getOrAwaitValue()

        // Assert they are different values
        assertNotEquals(initialValue, valueAfterRefresh)
        assertEquals(initialValue, FakeDataSource.CURRENT_VALUE)
        assertEquals(valueAfterRefresh, FakeDataSource.NEW_VALUE)
    }
}

@ExperimentalCoroutinesApi
class FakeDataSource : DataSource {

    companion object {
        const val CURRENT_VALUE = "test"
        const val CURRENT_TIME = 123456781234
        const val WEATHER_CONDITION = "Sunny test"
        const val NEW_VALUE = "new value"
    }

    private val _currentValue = MutableLiveData<String>(CURRENT_VALUE)
    override val cachedData: LiveData<String> = _currentValue

    override fun getCurrentTime(): LiveData<Long> = MutableLiveData<Long>(CURRENT_TIME)

    val testDispatcher = TestCoroutineDispatcher()
    override fun fetchWeather(): LiveData<String> = liveData(testDispatcher) {
        emit(WEATHER_CONDITION)
    }

    override suspend fun fetchNewData() {
        _currentValue.value = NEW_VALUE
    }
}