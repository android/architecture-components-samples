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
