package com.android.example.paging.pagingwithnetwork.reddit.ui

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.example.paging.pagingwithnetwork.reddit.db.RedditDb
import com.android.example.paging.pagingwithnetwork.reddit.repository.inDb.PageKeyedRemoteMediator
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import com.android.example.paging.pagingwithnetwork.repository.FakeRedditApi
import com.android.example.paging.pagingwithnetwork.repository.PostFactory
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalPagingApi
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PageKeyedRemoteMediatorTest {
    private val postFactory = PostFactory()
    private val mockPosts = listOf(
        postFactory.createRedditPost(SubRedditViewModel.DEFAULT_SUBREDDIT),
        postFactory.createRedditPost(SubRedditViewModel.DEFAULT_SUBREDDIT),
        postFactory.createRedditPost(SubRedditViewModel.DEFAULT_SUBREDDIT)
    )
    private val mockApi = FakeRedditApi()

    private val mockDb = RedditDb.create(
        ApplicationProvider.getApplicationContext(),
        useInMemory = true
    )

    @After
    fun tearDown() {
        mockDb.clearAllTables()
        // Clear out failure message to default to the successful response.
        mockApi.failureMsg = null
        // Clear out posts after each test run.
        mockApi.clearPosts()
    }

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        // Add mock results for the API to return.
        mockPosts.forEach { post -> mockApi.addPost(post) }
        val remoteMediator = PageKeyedRemoteMediator(
            mockDb,
            mockApi,
            SubRedditViewModel.DEFAULT_SUBREDDIT
        )
        val pagingState = PagingState<Int, RedditPost>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadSuccessAndEndOfPaginationWhenNoMoreData() = runTest {
        // To test endOfPaginationReached, don't set up the mockApi to return post
        // data here.
        val remoteMediator = PageKeyedRemoteMediator(
            mockDb,
            mockApi,
            SubRedditViewModel.DEFAULT_SUBREDDIT
        )
        val pagingState = PagingState<Int, RedditPost>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoadReturnsErrorResultWhenErrorOccurs() = runTest {
        // Set up failure message to throw exception from the mock API.
        mockApi.failureMsg = "Throw test failure"
        val remoteMediator = PageKeyedRemoteMediator(
            mockDb,
            mockApi,
            SubRedditViewModel.DEFAULT_SUBREDDIT
        )
        val pagingState = PagingState<Int, RedditPost>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }

}
