/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.example.paging.pagingwithnetwork.reddit.ui

import android.app.Application
import android.content.Intent
import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.test.InstrumentationRegistry
import androidx.recyclerview.widget.RecyclerView
import com.android.example.paging.pagingwithnetwork.R
import com.android.example.paging.pagingwithnetwork.reddit.DefaultServiceLocator
import com.android.example.paging.pagingwithnetwork.reddit.ServiceLocator
import com.android.example.paging.pagingwithnetwork.reddit.api.RedditApi
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import com.android.example.paging.pagingwithnetwork.reddit.ui.RedditActivity.Companion.DEFAULT_SUBREDDIT
import com.android.example.paging.pagingwithnetwork.repository.FakeRedditApi
import com.android.example.paging.pagingwithnetwork.repository.PostFactory
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Simple sanity test to ensure data is displayed
 */
@RunWith(Parameterized::class)
class RedditActivityTest(private val type: RedditPostRepository.Type) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun params() = RedditPostRepository.Type.values()
    }

    @get:Rule
    var testRule = CountingTaskExecutorRule()

    private val postFactory = PostFactory()
    @Before
    fun init() {
        val fakeApi = FakeRedditApi()
        fakeApi.addPost(postFactory.createRedditPost(DEFAULT_SUBREDDIT))
        fakeApi.addPost(postFactory.createRedditPost(DEFAULT_SUBREDDIT))
        fakeApi.addPost(postFactory.createRedditPost(DEFAULT_SUBREDDIT))
        val app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        // use a controlled service locator w/ fake API
        ServiceLocator.swap(
                object : DefaultServiceLocator(app = app,
                        useInMemoryDb = true) {
                    override fun getRedditApi(): RedditApi = fakeApi
                }
        )
    }

    @Test
    @Throws(InterruptedException::class, TimeoutException::class)
    fun showSomeResults() {
        val intent = RedditActivity.intentFor(
                context = InstrumentationRegistry.getTargetContext(),
                type = type)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent)
        val recyclerView = activity.findViewById<RecyclerView>(R.id.list)
        assertThat(recyclerView.adapter, notNullValue())
        waitForAdapterChange(recyclerView)
        assertThat(recyclerView.adapter?.itemCount, `is`(3))
    }

    private fun waitForAdapterChange(recyclerView: RecyclerView) {
        val latch = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            recyclerView.adapter?.registerAdapterDataObserver(
                    object : RecyclerView.AdapterDataObserver() {
                        override fun onChanged() {
                            latch.countDown()
                        }

                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            latch.countDown()
                        }
                    })
        }
        testRule.drainTasks(1, TimeUnit.SECONDS)
        if (recyclerView.adapter?.itemCount ?: 0 > 0) {
            return
        }
        assertThat(latch.await(10, TimeUnit.SECONDS), `is`(true))
    }
}