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
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPostPageKey
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PageKeyedRemoteMediator(
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
            // Get the closest item from PagingState that we want to load data around.
            val redditPost = when (loadType) {
                REFRESH -> null
                PREPEND -> state.pages
                    .firstOrNull { it.data.isNotEmpty() }
                    ?.data?.firstOrNull()
                APPEND -> state.pages
                    .lastOrNull { it.data.isNotEmpty() }
                    ?.data?.lastOrNull()
            }

            // Query DB for RedditPostPageKey corresponding to the item above.
            // RedditPostPageKey is a wrapper object we use to keep track of page keys we receive
            // from the Reddit API to fetch the next or previous page.
            val postKey = db.withTransaction {
                redditPost?.let { postDao.remoteKeyByPostName(it.name) }
            }

            // We must explicitly check if the page key is null when prepending or appending, since
            // the Reddit API informs the end of the list by returning null for page key, but
            // passing a null key to Reddit API will fetch the initial page.
            if (loadType == PREPEND && postKey?.prevPageKey == null) {
                return MediatorResult.Success(endOfPaginationReached = true)
            } else if (loadType == APPEND && postKey?.nextPageKey == null) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val data = redditApi.getTop(
                subreddit = subredditName,
                after = if (loadType == APPEND) postKey?.nextPageKey else null,
                before = if (loadType == PREPEND) postKey?.prevPageKey else null,
                limit = if (loadType == REFRESH) state.config.initialLoadSize else state.config.pageSize
            ).data

            val items = data.children.map { it.data }

            db.withTransaction {
                if (loadType == REFRESH) {
                    postDao.deleteBySubreddit(subredditName)
                    postDao.deleteRemoteKeys()
                }

                postDao.insertPostKeys(
                    items.map { RedditPostPageKey(it.name, data.before, data.after) }
                )

                postDao.insert(items)
            }

            return MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }
}
