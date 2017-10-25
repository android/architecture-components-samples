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

package com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import com.android.example.paging.pagingwithnetwork.reddit.repository.Listing
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byItem.InMemoryByItemRepository
import com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byPage.InMemoryByPageKeyRepository
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import com.android.example.paging.pagingwithnetwork.repository.FakeRedditApi
import com.android.example.paging.pagingwithnetwork.repository.PostFactory
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository.Type.IN_MEMORY_BY_ITEM
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository.Type.IN_MEMORY_BY_PAGE
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import java.util.concurrent.Executor

@RunWith(Parameterized::class)
class InMemoryRepositoryTest(type : RedditPostRepository.Type) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun params() = listOf(IN_MEMORY_BY_ITEM, IN_MEMORY_BY_PAGE)
    }
    @Suppress("unused")
    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()
    private val fakeApi = FakeRedditApi()
    private val networkExecutor = Executor { command -> command.run() }
    private val repository = when(type) {
        IN_MEMORY_BY_PAGE -> InMemoryByPageKeyRepository(
                redditApi = fakeApi,
                networkExecutor = networkExecutor)
        IN_MEMORY_BY_ITEM -> InMemoryByItemRepository(
                redditApi = fakeApi,
                networkExecutor = networkExecutor)
        else -> throw IllegalArgumentException()
    }
    private val postFactory = PostFactory()
    /**
     * asserts that empty list works fine
     */
    @Test
    fun emptyList() {
        val listing = repository.postsOfSubreddit("foo", 10)
        val pagedList = getPagedList(listing)
        assertThat(pagedList.size, `is`(0))
    }

    /**
     * asserts that a list w/ single item is loaded properly
     */
    @Test
    fun oneItem() {
        val post = postFactory.createRedditPost("foo")
        fakeApi.addPost(post)
        val listing = repository.postsOfSubreddit(subReddit = "foo", pageSize = 10)
        assertThat(getPagedList(listing), `is`(listOf(post)))
    }

    /**
     * asserts loading a full list in multiple pages
     */
    @Test
    fun verifyCompleteList() {
        val posts = (0..10).map { postFactory.createRedditPost("bar") }
        posts.forEach(fakeApi::addPost)
        val listing = repository.postsOfSubreddit(subReddit = "bar", pageSize = 3)
        // trigger loading of the whole list
        val pagedList = getPagedList(listing)
        pagedList.loadAround(posts.size - 1)
        assertThat(pagedList, `is`(posts))
    }

    /**
     * asserts the failure message when the initial load cannot complete
     */
    @Test
    fun failToLoadInitial() {
        fakeApi.failureMsg = "xxx"
        val listing = repository.postsOfSubreddit(subReddit = "bar", pageSize = 3)
        // trigger load
        getPagedList(listing)
        assertThat(getNetworkState(listing), `is`(NetworkState.error("xxx")))
    }

    /**
     * asserts the retry logic when initial load request fails
     */
    @Test
    fun retryInInitialLoad() {
        fakeApi.addPost(postFactory.createRedditPost("foo"))
        fakeApi.failureMsg = "xxx"
        val listing = repository.postsOfSubreddit(subReddit = "foo", pageSize = 3)
        // trigger load
        val pagedList = getPagedList(listing)
        assertThat(pagedList.size, `is`(0))

        @Suppress("UNCHECKED_CAST")
        val networkObserver = Mockito.mock(Observer::class.java) as Observer<NetworkState>
        listing.networkState.observeForever(networkObserver)
        fakeApi.failureMsg = null
        listing.retry()
        assertThat(pagedList.size, `is`(1 ))
        assertThat(getNetworkState(listing), `is`(NetworkState.LOADED))
        val inOrder = Mockito.inOrder(networkObserver)
        inOrder.verify(networkObserver).onChanged(NetworkState.error("xxx"))
        inOrder.verify(networkObserver).onChanged(NetworkState.LOADING)
        inOrder.verify(networkObserver).onChanged(NetworkState.LOADED)
        inOrder.verifyNoMoreInteractions()
    }

    /**
     * asserts the retry logic when initial load succeeds but subsequent loads fails
     */
    @Test
    fun retryAfterInitialFails() {
        val posts = (0..10).map { postFactory.createRedditPost("bar") }
        posts.forEach(fakeApi::addPost)
        val listing = repository.postsOfSubreddit(subReddit = "bar", pageSize = 2)
        val list = getPagedList(listing)
        assertThat("test sanity, we should not load everything",
                list.size < posts.size, `is`(true))
        assertThat(getNetworkState(listing), `is`(NetworkState.LOADED))
        fakeApi.failureMsg = "fail"
        list.loadAround(posts.size - 1)
        assertThat(getNetworkState(listing), `is`(NetworkState.error("fail")))
        fakeApi.failureMsg = null
        listing.retry()
        assertThat(getNetworkState(listing), `is`(NetworkState.LOADED))
        assertThat(list, `is`(posts))
    }

    /**
     * asserts refresh loads the new data
     */
    @Test
    fun refresh() {
        val postsV1 = (0..5).map { postFactory.createRedditPost("bar") }
        postsV1.forEach(fakeApi::addPost)
        val listing = repository.postsOfSubreddit(subReddit = "bar", pageSize = 5)
        val list = getPagedList(listing)
        list.loadAround(10)
        val postsV2 = (0..10).map { postFactory.createRedditPost("bar") }
        fakeApi.clear()
        postsV2.forEach(fakeApi::addPost)

        @Suppress("UNCHECKED_CAST")
        val refreshObserver = Mockito.mock(Observer::class.java) as Observer<NetworkState>
        listing.refreshState.observeForever(refreshObserver)
        listing.refresh()

        val list2 = getPagedList(listing)
        list2.loadAround(10)
        assertThat(list2, `is`(postsV2))
        val inOrder = Mockito.inOrder(refreshObserver)
        inOrder.verify(refreshObserver).onChanged(NetworkState.LOADED) // initial state
        inOrder.verify(refreshObserver).onChanged(NetworkState.LOADING)
        inOrder.verify(refreshObserver).onChanged(NetworkState.LOADED)
    }

    /**
     * asserts that refresh also works after failure
     */
    @Test
    fun refreshAfterFailure() {
        val posts = (0..5).map { postFactory.createRedditPost("bar") }
        posts.forEach(fakeApi::addPost)

        fakeApi.failureMsg = "xx"
        val listing = repository.postsOfSubreddit(subReddit = "bar", pageSize = 5)
        getPagedList(listing)
        assertThat(getNetworkState(listing), `is`(NetworkState.error("xx")))
        fakeApi.failureMsg = null
        listing.refresh()
        // get the new list since refresh will create a new paged list
        assertThat(getPagedList(listing), `is`(posts))
    }

    /**
     * extract the latest paged list from the listing
     */
    private fun getPagedList(listing: Listing<RedditPost>): PagedList<RedditPost> {
        val observer = LoggingObserver<PagedList<RedditPost>>()
        listing.pagedList.observeForever(observer)
        assertThat(observer.value, `is`(notNullValue()))
        return observer.value!!
    }

    /**
     * extract the latest network state from the listing
     */
    private fun getNetworkState(listing: Listing<RedditPost>) : NetworkState? {
        val networkObserver = LoggingObserver<NetworkState>()
        listing.networkState.observeForever(networkObserver)
        return networkObserver.value
    }

    /**
     * simple observer that logs the latest value it receives
     */
    private class LoggingObserver<T> : Observer<T> {
        var value : T? = null
        override fun onChanged(t: T?) {
            this.value = t
        }
    }
}