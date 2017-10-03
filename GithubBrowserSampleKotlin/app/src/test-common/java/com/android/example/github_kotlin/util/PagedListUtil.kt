package com.android.example.github_kotlin.util

import android.arch.core.executor.AppToolkitTaskExecutor
import android.arch.paging.PagedList
import android.arch.paging.TiledDataSource

/**
 * helper methods to create static paged lists
 */
object PagedListUtil {
    fun <T> from(source: List<T>): PagedList<T> {
        return PagedList.Builder<Int, T>()
                .setBackgroundThreadExecutor(AppToolkitTaskExecutor.getIOThreadExecutor())
                .setMainThreadExecutor(AppToolkitTaskExecutor.getMainThreadExecutor())
                .setDataSource(object : TiledDataSource<T>() {

                    override fun countItems(): Int {
                        return source.size
                    }

                    override fun loadRange(startPosition: Int, count: Int): List<T> {
                        val end = Math.min(source.size, startPosition + count)
                        return source.subList(startPosition, end)
                    }
                })
                .setConfig(PagedList.Config.Builder()
                                   .setEnablePlaceholders(true)
                                   .setInitialLoadSizeHint(Math.max(source.size, 1))
                                   .setPageSize(Math.max(source.size, 1)).build()).build()

    }
}
