/*
 * Copyright 2019 The Android Open Source Project
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
package com.example.benchmark

import androidx.benchmark.BenchmarkRule
import androidx.test.annotation.UiThreadTest
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import kotlinx.android.synthetic.main.activity_benchmark.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PostsAdapterBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    val activityRule = ActivityTestRule(BenchmarkActivity::class.java)

    @Before
    fun setup() {
        activityRule.runOnUiThread {
            // Ensure surrounding pages are loaded before starting benchmarks.
            activityRule.activity.list.scrollTo(0, 1000)
        }
    }

    @UiThreadTest
    @Test
    fun scrollItem() {
        var position = 1
        benchmarkRule.measure {
            activityRule.activity.list.scrollToPosition(position)
            position += 1
        }
    }

    @UiThreadTest
    @Test
    fun scrollPage() {
        var position = 5
        benchmarkRule.measure {
            activityRule.activity.list.scrollToPosition(0)
            position += 5
        }
    }
}
