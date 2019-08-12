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

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.recyclerview.widget.RecyclerView
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import kotlinx.android.synthetic.main.activity_benchmark.*
import org.junit.Assert.assertTrue
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

    @UiThreadTest
    @Test
    fun scrollItem() {
        val activity = activityRule.activity

        // If RecyclerView has children, the items are attached, bound, and gone through layout.
        // Ready to benchmark.
        assertTrue("RecyclerView expected to have children", activity.list.childCount > 0)

        benchmarkRule.measureRepeated {
            activity.list.scrollByOneItem()
            runWithTimingDisabled {
                activity.testExecutor.flush()
            }
        }
    }

    private fun RecyclerView.scrollByOneItem() {
        scrollBy(0, getChildAt(childCount - 1).height)
    }
}
