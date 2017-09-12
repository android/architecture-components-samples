package com.android.example.github.util;

import android.arch.core.executor.AppToolkitTaskExecutor;
import android.arch.paging.PagedList;
import android.arch.paging.TiledDataSource;

import java.util.List;

/**
 * helper methods to create static paged lists
 */
public class PagedListUtil {
    public static <T> PagedList<T> from(List<T> source) {
        return new PagedList.Builder<Integer, T>()
                .setBackgroundThreadExecutor(AppToolkitTaskExecutor.getIOThreadExecutor())
                .setMainThreadExecutor(AppToolkitTaskExecutor.getMainThreadExecutor())
                .setDataSource(new TiledDataSource<T>() {
                    @Override
                    public int loadCount() {
                        return source.size();
                    }

                    @Override
                    public List<T> loadRange(int startPosition, int count) {
                        int end = Math.min(source.size(), startPosition + count);
                        return source.subList(startPosition, end);
                    }
                })
                .setConfig(new PagedList.Config.Builder()
                    .setEnablePlaceholders(true)
                    .setInitialLoadSizeHint(Math.max(source.size(), 1))
                    .setPageSize(Math.max(source.size(), 1)).build()).build();

    }
}
