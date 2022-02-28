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
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
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
    private val fakeDataSource = FakeDataSource(mainCoroutineRule.testDispatcher)

    // Class under test. Uses Dispatchers.Main so that the MainCoroutineRule can control it.
    private lateinit var viewModel: LiveDataViewModel

    @Before
    fun initViewModel() {
        // Initialize the ViewModel after the [MainCoroutineRule] is applied so that it has the
        // right test dispatcher.
        viewModel = LiveDataViewModel(fakeDataSource)
    }

    @Test
    fun getCurrentTime_notEmpty() {
        val cachedValue = viewModel.cachedValue.getOrAwaitValue()
        assertTrue(cachedValue.isNotEmpty())
    }

    @Test
    fun currentTimeTransformed() = runTest {
        // Get the result of a coroutine inside a transformation
        val timeTransformed = viewModel.currentTimeTransformed.getOrAwaitValue {
            // After observing, advance the clock to avoid the delay calls.
            advanceUntilIdle()
        }
        assertEquals(Date(FakeDataSource.CURRENT_TIME).toString(), timeTransformed)
    }

    @Test
    fun getCurrentWeather_loading() = runTest {
        // Keep observing currentWeather
        viewModel.currentWeather.observeForTesting {
            // Yield test thread so that the first LiveData emission can complete
            yield()

            // Verify that the first value is Loading
            assertEquals(LiveDataViewModel.LOADING_STRING, viewModel.currentWeather.value)

            // Execute all pending coroutines in the viewModel
            runCurrent()

            // Verify the new value is available
            assertEquals(FakeDataSource.WEATHER_CONDITION, viewModel.currentWeather.value)
        }
    }

    @Test
    fun cache_RefreshFromViewModelScope() = runTest {
        // Get the initial value that comes directly from FakeDataSource
        val initialValue = viewModel.cachedValue.getOrAwaitValue()

        // Trigger an update, which starts a coroutine that updates the value
        viewModel.onRefresh()

        // Run pending coroutine in ViewModel
        runCurrent()

        // Get the new value
        val valueAfterRefresh = viewModel.cachedValue.getOrAwaitValue()

        // Assert they are different values
        assertNotEquals(initialValue, valueAfterRefresh)
        assertEquals(FakeDataSource.CURRENT_VALUE, initialValue)
        assertEquals(FakeDataSource.NEW_VALUE, valueAfterRefresh)
    }
}

@ExperimentalCoroutinesApi
class FakeDataSource(private val testDispatcher: TestDispatcher) : DataSource {

    companion object {
        const val CURRENT_VALUE = "test"
        const val CURRENT_TIME = 123456781234
        const val WEATHER_CONDITION = "Sunny test"
        const val NEW_VALUE = "new value"
    }

    private val _currentValue = MutableLiveData<String>(CURRENT_VALUE)
    override val cachedData: LiveData<String> = _currentValue

    override fun getCurrentTime(): LiveData<Long> = MutableLiveData<Long>(CURRENT_TIME)

    override fun fetchWeather(): LiveData<String> = liveData(testDispatcher) {
        emit(WEATHER_CONDITION)
    }

    override suspend fun fetchNewData() {
        _currentValue.value = NEW_VALUE
    }
}