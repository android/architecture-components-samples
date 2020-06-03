package com.android.example.paging.pagingwithnetwork.reddit.repository.inDb

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.android.example.paging.pagingwithnetwork.reddit.api.RedditApi
import com.android.example.paging.pagingwithnetwork.reddit.db.RedditDb
import com.android.example.paging.pagingwithnetwork.reddit.db.RedditPostDao
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ItemKeyedRemoteMediator(
    private val db: RedditDb,
    private val redditApi: RedditApi,
    private val subredditName: String
) : RemoteMediator<Int, RedditPost>() {
    private val postDao: RedditPostDao = db.posts()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RedditPost>
    ): MediatorResult {
        try {
            val items = redditApi.getTop(
                subreddit = subredditName,
                after = when (loadType) {
                    APPEND -> state.pages
                        .lastOrNull { it.data.isNotEmpty() }
                        ?.data?.lastOrNull()?.name
                    else -> null
                },
                before = when (loadType) {
                    PREPEND -> state.pages
                        .firstOrNull { it.data.isNotEmpty() }
                        ?.data?.firstOrNull()?.name
                    else -> null
                },
                limit = state.config.pageSize
            ).data.children.map { it.data }

            db.withTransaction {
                if (loadType == REFRESH) {
                    postDao.deleteBySubreddit(subredditName)
                }

                postDao.insert(items)
            }

            return MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}