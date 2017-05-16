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

package com.android.example.github.ui.user;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.repository.UserRepository;
import com.android.example.github.util.TestUtil;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class UserViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private UserViewModel userViewModel;
    private UserRepository userRepository;
    private RepoRepository repoRepository;

    @Before
    public void setup() {
        userRepository = mock(UserRepository.class);
        repoRepository = mock(RepoRepository.class);
        userViewModel = new UserViewModel(userRepository, repoRepository);
    }

    @Test
    public void testNull() {
        assertThat(userViewModel.getUser(), notNullValue());
        verify(userRepository, never()).loadUser(anyString());
        userViewModel.setLogin("foo");
        verify(userRepository, never()).loadUser(anyString());
    }

    @Test
    public void testCallRepo() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        userViewModel.getUser().observeForever(mock(Observer.class));
        userViewModel.setLogin("abc");
        verify(userRepository).loadUser(captor.capture());
        assertThat(captor.getValue(), is("abc"));
        reset(userRepository);
        userViewModel.setLogin("ddd");
        verify(userRepository).loadUser(captor.capture());
        assertThat(captor.getValue(), is("ddd"));
    }

    @Test
    public void sendResultToUI() {
        MutableLiveData<Resource<User>> foo = new MutableLiveData<>();
        MutableLiveData<Resource<User>> bar = new MutableLiveData<>();
        when(userRepository.loadUser("foo")).thenReturn(foo);
        when(userRepository.loadUser("bar")).thenReturn(bar);
        Observer<Resource<User>> observer = mock(Observer.class);
        userViewModel.getUser().observeForever(observer);
        userViewModel.setLogin("foo");
        verify(observer, never()).onChanged(any(Resource.class));
        User fooUser = TestUtil.createUser("foo");
        Resource<User> fooValue = Resource.success(fooUser);

        foo.setValue(fooValue);
        verify(observer).onChanged(fooValue);
        reset(observer);
        User barUser = TestUtil.createUser("bar");
        Resource<User> barValue = Resource.success(barUser);
        bar.setValue(barValue);
        userViewModel.setLogin("bar");
        verify(observer).onChanged(barValue);
    }

    @Test
    public void loadRepositories() {
        userViewModel.getRepositories().observeForever(mock(Observer.class));
        verifyNoMoreInteractions(repoRepository);
        userViewModel.setLogin("foo");
        verify(repoRepository).loadRepos("foo");
        reset(repoRepository);
        userViewModel.setLogin("bar");
        verify(repoRepository).loadRepos("bar");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void retry() {
        userViewModel.setLogin("foo");
        verifyNoMoreInteractions(repoRepository, userRepository);
        userViewModel.retry();
        verifyNoMoreInteractions(repoRepository, userRepository);
        Observer userObserver = mock(Observer.class);
        userViewModel.getUser().observeForever(userObserver);
        Observer repoObserver = mock(Observer.class);
        userViewModel.getRepositories().observeForever(repoObserver);

        verify(userRepository).loadUser("foo");
        verify(repoRepository).loadRepos("foo");
        reset(userRepository, repoRepository);

        userViewModel.retry();
        verify(userRepository).loadUser("foo");
        verify(repoRepository).loadRepos("foo");
        reset(userRepository, repoRepository);
        userViewModel.getUser().removeObserver(userObserver);
        userViewModel.getRepositories().removeObserver(repoObserver);

        userViewModel.retry();
        verifyNoMoreInteractions(userRepository, repoRepository);
    }

    @Test
    public void nullUser() {
        Observer<Resource<User>> observer = mock(Observer.class);
        userViewModel.setLogin("foo");
        userViewModel.setLogin(null);
        userViewModel.getUser().observeForever(observer);
        verify(observer).onChanged(null);
    }

    @Test
    public void nullRepoList() {
        Observer<Resource<List<Repo>>> observer = mock(Observer.class);
        userViewModel.setLogin("foo");
        userViewModel.setLogin(null);
        userViewModel.getRepositories().observeForever(observer);
        verify(observer).onChanged(null);
    }

    @Test
    public void dontRefreshOnSameData() {
        Observer<String> observer = mock(Observer.class);
        userViewModel.login.observeForever(observer);
        verifyNoMoreInteractions(observer);
        userViewModel.setLogin("foo");
        verify(observer).onChanged("foo");
        reset(observer);
        userViewModel.setLogin("foo");
        verifyNoMoreInteractions(observer);
        userViewModel.setLogin("bar");
        verify(observer).onChanged("bar");
    }

    @Test
    public void noRetryWithoutUser() {
        userViewModel.retry();
        verifyNoMoreInteractions(userRepository, repoRepository);
    }
}