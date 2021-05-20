package com.android.example.paging.pagingwithnetwork.reddit.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.*
import androidx.paging.LoadStates
import com.android.example.paging.pagingwithnetwork.reddit.paging.SynchronousRemoteState.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import kotlin.Error

/**
 * Track the combined [LoadState] of [RemoteMediator] and [PagingSource], so that each load type
 * is only set to [NotLoading] when [RemoteMediator] load is applied on presenter-side.
 */
private class SynchronousRemoteStates {
    var refresh: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var prepend: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var append: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    private var refreshState: SynchronousRemoteState = NOT_LOADING
    private var prependState: SynchronousRemoteState = NOT_LOADING
    private var appendState: SynchronousRemoteState = NOT_LOADING

    fun toLoadStates() = LoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append
    )

    internal fun updateFromCombinedLoadStates(combinedLoadStates: CombinedLoadStates) {
        computeSynchronousRemoteStates(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.refresh,
            remoteState = combinedLoadStates.mediator?.refresh,
            synchronousRemoteState = refreshState,
        ).also {
            refresh = it.first
            refreshState = it.second
        }
        computeSynchronousRemoteStates(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.prepend,
            remoteState = combinedLoadStates.mediator?.prepend,
            synchronousRemoteState = prependState,
        ).also {
            prepend = it.first
            prependState = it.second
        }
        computeSynchronousRemoteStates(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.append,
            remoteState = combinedLoadStates.mediator?.append,
            synchronousRemoteState = appendState,
        ).also {
            append = it.first
            appendState = it.second
        }
    }

    private fun computeSynchronousRemoteStates(
        sourceRefreshState: LoadState,
        sourceState: LoadState,
        remoteState: LoadState?,
        synchronousRemoteState: SynchronousRemoteState,
    ): Pair<LoadState, SynchronousRemoteState> {
        if (remoteState == null) return sourceState to NOT_LOADING

        return when (synchronousRemoteState) {
            NOT_LOADING -> when (remoteState) {
                is Loading -> Loading to REMOTE_STARTED
                is Error -> remoteState to REMOTE_ERROR
                else -> NotLoading(remoteState.endOfPaginationReached) to NOT_LOADING
            }
            REMOTE_STARTED -> when {
                remoteState is Error -> remoteState to REMOTE_ERROR
                sourceRefreshState is Loading -> Loading to SOURCE_LOADING
                else -> Loading to REMOTE_STARTED
            }
            REMOTE_ERROR -> when (remoteState) {
                is Error -> remoteState to REMOTE_ERROR
                else -> Loading to REMOTE_STARTED
            }
            SOURCE_LOADING -> when {
                sourceRefreshState is Error -> sourceRefreshState to SOURCE_ERROR
                remoteState is Error -> remoteState to REMOTE_ERROR
                sourceRefreshState is NotLoading -> {
                    NotLoading(remoteState.endOfPaginationReached) to NOT_LOADING
                }
                else -> Loading to SOURCE_LOADING
            }
            SOURCE_ERROR -> when (sourceRefreshState) {
                is Error -> sourceRefreshState to SOURCE_ERROR
                else -> sourceRefreshState to SOURCE_LOADING
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<CombinedLoadStates>.asSynchronousRemoteStates(): Flow<LoadStates> {
    val syncRemoteState = SynchronousRemoteStates()
    return scan(syncRemoteState.toLoadStates()) { _, combinedLoadStates ->
        syncRemoteState.updateFromCombinedLoadStates(combinedLoadStates)
        syncRemoteState.toLoadStates()
    }
}

/**
 * State machine used to compute [LoadState] values in [SynchronousRemoteStates].
 */
enum class SynchronousRemoteState {
    /**
     * Idle state; defer to remote state for endOfPaginationReached.
     */
    NOT_LOADING,

    /**
     * Remote load triggered; start listening for source refresh.
     */
    REMOTE_STARTED,

    /**
     * Waiting for remote in error state to get retried
     */
    REMOTE_ERROR,

    /**
     * Source refresh triggered by remote invalidation, once this completes we can be sure
     * the next generation was loaded.
     */
    SOURCE_LOADING,

    /**
     *  Remote load completed, but waiting for source refresh in error state to get retried.
     */
    SOURCE_ERROR,
}
