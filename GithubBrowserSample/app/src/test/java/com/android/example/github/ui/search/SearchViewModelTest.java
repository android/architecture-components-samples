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
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SearchViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();
    private SearchViewModel viewModel;
    private RepoRepository repository;
    @Before
    public void init() {
        repository = mock(RepoRepository.class);
        viewModel = new SearchViewModel(repository);
    }

    @Test
    public void empty() {
        Observer<Resource<List<Repo>>> result = mock(Observer.class);
        viewModel.getResults().observeForever(result);
        viewModel.loadNextPage();
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void basic() {
        Observer<Resource<List<Repo>>> result = mock(Observer.class);
        viewModel.getResults().observeForever(result);
        viewModel.setQuery("foo");
        verify(repository).search("foo");
        verify(repository, never()).searchNextPage("foo");
    }

    @Test
    public void noObserverNoQuery() {
        when(repository.searchNextPage("foo")).thenReturn(mock(LiveData.class));
        viewModel.setQuery("foo");
        verify(repository, never()).search("foo");
        // next page is user interaction and even if loading state is not observed, we query
        // would be better to avoid that if main search query is not observed
        viewModel.loadNextPage();
        verify(repository).searchNextPage("foo");
    }

    @Test
    public void swap() {
        LiveData<Resource<Boolean>> nextPage = new MutableLiveData<>();
        when(repository.searchNextPage("foo")).thenReturn(nextPage);

        Observer<Resource<List<Repo>>> result = mock(Observer.class);
        viewModel.getResults().observeForever(result);
        verifyNoMoreInteractions(repository);
        viewModel.setQuery("foo");
        verify(repository).search("foo");
        viewModel.loadNextPage();

        viewModel.getLoadMoreStatus().observeForever(mock(Observer.class));
        verify(repository).searchNextPage("foo");
        assertThat(nextPage.hasActiveObservers(), is(true));
        viewModel.setQuery("bar");
        assertThat(nextPage.hasActiveObservers(), is(false));
        verify(repository).search("bar");
        verify(repository, never()).searchNextPage("bar");
    }

    @Test
    public void refresh() {
        viewModel.refresh();
        verifyNoMoreInteractions(repository);
        viewModel.setQuery("foo");
        viewModel.refresh();
        verifyNoMoreInteractions(repository);
        viewModel.getResults().observeForever(mock(Observer.class));
        verify(repository).search("foo");
        reset(repository);
        viewModel.refresh();
        verify(repository).search("foo");
    }

    @Test
    public void resetSameQuery() {
        viewModel.getResults().observeForever(mock(Observer.class));
        viewModel.setQuery("foo");
        verify(repository).search("foo");
        reset(repository);
        viewModel.setQuery("FOO");
        verifyNoMoreInteractions(repository);
        viewModel.setQuery("bar");
        verify(repository).search("bar");
    }
}