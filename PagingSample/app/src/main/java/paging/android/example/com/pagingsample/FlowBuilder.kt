package paging.android.example.com.pagingsample

import androidx.annotation.AnyThread
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedList.BoundaryCallback
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FlowBuilder<Key, Value>(
    dataSourceFactory: DataSource.Factory<Key, Value>,
    config: PagedList.Config
) {
    private var mInitialLoadKey: Key? = null
    private val mConfig: PagedList.Config
    private val mDataSourceFactory: DataSource.Factory<Key, Value>
    private var mBoundaryCallback: BoundaryCallback<*>? = null

    constructor(
        dataSourceFactory: DataSource.Factory<Key, Value>,
        pageSize: Int
    ) : this(dataSourceFactory, PagedList.Config.Builder().setPageSize(pageSize).build())

    fun setInitialLoadKey(key: Key?): FlowBuilder<Key, Value> {
        mInitialLoadKey = key
        return this
    }

    fun setBoundaryCallback(
        boundaryCallback: BoundaryCallback<Value>?
    ): FlowBuilder<Key, Value> {
        mBoundaryCallback = boundaryCallback
        return this
    }

    fun setFetchExecutor(
        fetchExecutor: Executor
    ): FlowBuilder<Key, Value> {
        // TODO
        return this
    }

    fun build(): Flow<PagedList<Value>> {
        return create(mInitialLoadKey, mConfig, mBoundaryCallback, mDataSourceFactory)
    }

    companion object {
        @AnyThread
        private fun <Key, Value> create(
            initialLoadKey: Key?,
            config: PagedList.Config,
            boundaryCallback: BoundaryCallback<*>?,
            dataSourceFactory: DataSource.Factory<Key, Value>
        ): Flow<PagedList<Value>> {

            var lastList: PagedList<Value>? = null
            var lastDataSource: DataSource<Key, Value>? = null

            val invalidateChannel = ConflatedBroadcastChannel(Unit)
            val mCallback = DataSource.InvalidatedCallback { invalidateChannel.offer(Unit) }

            fun disconnectLastDataSourceCallback() {
                lastDataSource?.run { removeInvalidatedCallback(mCallback) }
            }

            return invalidateChannel.asFlow().map {
                disconnectLastDataSourceCallback()

                val initializeKey = lastList?.run { (lastList as PagedList<Value>).lastKey as Key } ?: initialLoadKey
                val currentDataSource = dataSourceFactory.create()
                currentDataSource.addInvalidatedCallback(mCallback)

                val list = PagedList.Builder(currentDataSource, config)
                    .setNotifyExecutor(Executors.newSingleThreadExecutor()) // TODO
                    .setFetchExecutor(Executors.newSingleThreadExecutor()) // TODO
                    .setBoundaryCallback(boundaryCallback)
                    .setInitialKey(initializeKey)
                    .build()

                lastList = list
                lastDataSource = currentDataSource

                list
            }.onCompletion {
                disconnectLastDataSourceCallback()
            }
        }
    }

    /**
     * Creates a LivePagedListBuilder with required parameters.
     *
     * @param dataSourceFactory DataSource factory providing DataSource generations.
     * @param config Paging configuration.
     */
    init {
        requireNotNull(config) { "PagedList.Config must be provided" }
        requireNotNull(dataSourceFactory) { "DataSource.Factory must be provided" }
        mDataSourceFactory = dataSourceFactory
        mConfig = config
    }
}