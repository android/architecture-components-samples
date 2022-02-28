package com.android.example.paging.pagingwithnetwork.reddit.repository

import androidx.paging.PagingSource.LoadParams.Refresh
import androidx.paging.PagingSource.LoadResult.Page
import com.android.example.paging.pagingwithnetwork.reddit.ui.SubRedditViewModel.Companion.DEFAULT_SUBREDDIT
import com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byItem.ItemKeyedSubredditPagingSource
import com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.byPage.PageKeyedSubredditPagingSource
import com.android.example.paging.pagingwithnetwork.repository.FakeRedditApi
import com.android.example.paging.pagingwithnetwork.repository.PostFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SubredditPagingSourceTest {
    private val postFactory = PostFactory()
    private val fakePosts = listOf(
        postFactory.createRedditPost(DEFAULT_SUBREDDIT),
        postFactory.createRedditPost(DEFAULT_SUBREDDIT),
        postFactory.createRedditPost(DEFAULT_SUBREDDIT)
    )
    private val fakeApi = FakeRedditApi().apply {
        fakePosts.forEach { post -> addPost(post) }
    }

    @Test
    fun itemKeyedSubredditPagingSource() = runTest {
        val pagingSource = ItemKeyedSubredditPagingSource(fakeApi, DEFAULT_SUBREDDIT)
        assertEquals(
            expected = Page(
                data = listOf(fakePosts[0], fakePosts[1]),
                prevKey = fakePosts[0].name,
                nextKey = fakePosts[1].name
            ),
            actual = pagingSource.load(
                Refresh(
                    key = null,
                    loadSize = 2,
                    placeholdersEnabled = false
                )
            ),
        )
    }

    @Test
    fun pageKeyedSubredditPagingSource() = runTest {
        val pagingSource = PageKeyedSubredditPagingSource(fakeApi, DEFAULT_SUBREDDIT)
        assertEquals(
            expected = Page(
                data = listOf(fakePosts[0], fakePosts[1]),
                prevKey = null,
                nextKey = fakePosts[1].name
            ),
            actual = pagingSource.load(
                Refresh(
                    key = null,
                    loadSize = 2,
                    placeholdersEnabled = false
                )
            ),
        )
    }
}