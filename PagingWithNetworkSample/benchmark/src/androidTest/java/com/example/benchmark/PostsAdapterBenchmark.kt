package com.example.benchmark

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.benchmark.BenchmarkRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.annotation.UiThreadTest
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.android.example.paging.pagingwithnetwork.reddit.ui.SubRedditViewModel
import kotlinx.android.synthetic.main.activity_benchmark.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class PostsAdapterBenchmark {

    @get:Rule
    var testRule = CountingTaskExecutorRule()

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    val activityRule = ActivityTestRule(BenchmarkActivity::class.java)

    @Before
    fun setup() {
        activityRule.runOnUiThread {
            // Ensure surrounding items are loaded before starting benchmarks.
            val recyclerView = activityRule.activity.list
            recyclerView.scrollToPosition(100)
            waitForAdapterChange(recyclerView)
            recyclerView.scrollToPosition(100 + SubRedditViewModel.PAGE_SIZE)
        }
    }

    @UiThreadTest
    @Test
    fun scrollItem() {
        var position = 1
        benchmarkRule.measure {
            activityRule.activity.list.scrollToPosition(position)
            position = (position % SubRedditViewModel.PAGE_SIZE) + 1
        }
    }

    @UiThreadTest
    @Test
    fun scrollPage() {
        val recyclerView = activityRule.activity.list
        var position = SubRedditViewModel.PAGE_SIZE
        benchmarkRule.measure {
            recyclerView.scrollToPosition(position)
            waitForAdapterChange(recyclerView)
            position += SubRedditViewModel.PAGE_SIZE
        }
    }

    private fun waitForAdapterChange(recyclerView: RecyclerView) {
        val latch = CountDownLatch(1)
        recyclerView.adapter?.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        latch.countDown()
                    }

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        latch.countDown()
                    }
                })
        testRule.drainTasks(1, TimeUnit.SECONDS)
        if (recyclerView.adapter?.itemCount ?: 0 > 0) {
            return
        }
        assertThat(latch.await(10, TimeUnit.SECONDS), `is`(true))
    }
}
