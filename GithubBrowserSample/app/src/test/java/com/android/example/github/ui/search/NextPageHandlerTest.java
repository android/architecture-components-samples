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

package com.android.example.github.ui.search;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.MutableLiveData;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class NextPageHandlerTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private SearchViewModel.NextPageHandler pageHandler;

    private RepoRepository repository;

    @Before
    public void init() {
        repository = mock(RepoRepository.class);
        pageHandler = new SearchViewModel.NextPageHandler(repository);
    }

    @Test
    public void constructor() {
        SearchViewModel.LoadMoreState initial = getStatus();
        assertThat(initial, notNullValue());
        assertThat(initial.isRunning(), is(false));
        assertThat(initial.getErrorMessage(), nullValue());
    }

    @Test
    public void reloadSameValue() {
        enqueueResponse("foo");
        pageHandler.queryNextPage("foo");
        verify(repository).searchNextPage("foo");

        reset(repository);
        pageHandler.queryNextPage("foo");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void success() {
        MutableLiveData<Resource<Boolean>> liveData = enqueueResponse("foo");

        pageHandler.queryNextPage("foo");
        verify(repository).searchNextPage("foo");
        assertThat(liveData.hasActiveObservers(), is(true));
        pageHandler.onChanged(Resource.loading(null));
        assertThat(liveData.hasActiveObservers(), is(true));
        assertThat(getStatus().isRunning(), is(true));

        pageHandler.onChanged(Resource.success(true));
        assertThat(liveData.hasActiveObservers(), is(false));
        assertThat(pageHandler.hasMore, is(true));
        assertThat(getStatus().isRunning(), is(false));
        assertThat(liveData.hasActiveObservers(), is(false));

        // requery
        reset(repository);
        MutableLiveData<Resource<Boolean>> nextPage = enqueueResponse("foo");
        pageHandler.queryNextPage("foo");
        verify(repository).searchNextPage("foo");
        assertThat(nextPage.hasActiveObservers(), is(true));

        pageHandler.onChanged(Resource.success(false));
        assertThat(liveData.hasActiveObservers(), is(false));
        assertThat(pageHandler.hasMore, is(false));
        assertThat(getStatus().isRunning(), is(false));
        assertThat(nextPage.hasActiveObservers(), is(false));

        // retry, no query
        reset(repository);
        pageHandler.queryNextPage("foo");
        verifyNoMoreInteractions(repository);
        pageHandler.queryNextPage("foo");
        verifyNoMoreInteractions(repository);

        // query another
        MutableLiveData<Resource<Boolean>> bar = enqueueResponse("bar");
        pageHandler.queryNextPage("bar");
        verify(repository).searchNextPage("bar");
        assertThat(bar.hasActiveObservers(), is(true));
    }

    @Test
    public void failure() {
        MutableLiveData<Resource<Boolean>> liveData = enqueueResponse("foo");
        pageHandler.queryNextPage("foo");
        assertThat(liveData.hasActiveObservers(), is(true));
        pageHandler.onChanged(Resource.error("idk", false));
        assertThat(liveData.hasActiveObservers(), is(false));
        assertThat(getStatus().getErrorMessage(), is("idk"));
        assertThat(getStatus().getErrorMessageIfNotHandled(), is("idk"));
        assertThat(getStatus().getErrorMessageIfNotHandled(), nullValue());
        assertThat(getStatus().isRunning(), is(false));
        assertThat(pageHandler.hasMore, is(true));

        reset(repository);
        MutableLiveData<Resource<Boolean>> liveData2 = enqueueResponse("foo");
        pageHandler.queryNextPage("foo");
        assertThat(liveData2.hasActiveObservers(), is(true));
        assertThat(getStatus().isRunning(), is(true));
        pageHandler.onChanged(Resource.success(false));
        assertThat(getStatus().isRunning(), is(false));
        assertThat(getStatus().getErrorMessage(), is(nullValue()));
        assertThat(pageHandler.hasMore, is(false));
    }

    @Test
    public void nullOnChanged() {
        MutableLiveData<Resource<Boolean>> liveData = enqueueResponse("foo");
        pageHandler.queryNextPage("foo");
        assertThat(liveData.hasActiveObservers(), is(true));
        pageHandler.onChanged(null);
        assertThat(liveData.hasActiveObservers(), is(false));
    }

    private SearchViewModel.LoadMoreState getStatus() {
        return pageHandler.getLoadMoreState().getValue();
    }

    private MutableLiveData<Resource<Boolean>> enqueueResponse(String query) {
        MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        when(repository.searchNextPage(query)).thenReturn(liveData);
        return liveData;
    }
}