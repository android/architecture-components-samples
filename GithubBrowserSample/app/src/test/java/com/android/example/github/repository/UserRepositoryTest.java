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

package com.android.example.github.repository;

import com.android.example.github.AppExecutors;
import com.android.example.github.api.ApiResponse;
import com.android.example.github.api.GithubService;
import com.android.example.github.db.UserDao;
import com.android.example.github.util.ApiUtil;
import com.android.example.github.util.InstantAppExecutors;
import com.android.example.github.util.TestUtil;
import com.android.example.github.vo.Resource;
import com.android.example.github.vo.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UserRepositoryTest {
    private UserDao userDao;
    private GithubService githubService;
    private UserRepository repo;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        userDao = mock(UserDao.class);
        githubService = mock(GithubService.class);
        repo = new UserRepository(new InstantAppExecutors(), userDao, githubService);
    }

    @Test
    public void loadUser() {
        repo.loadUser("abc");
        verify(userDao).findByLogin("abc");
    }

    @Test
    public void goToNetwork() {
        MutableLiveData<User> dbData = new MutableLiveData<>();
        when(userDao.findByLogin("foo")).thenReturn(dbData);
        User user = TestUtil.createUser("foo");
        LiveData<ApiResponse<User>> call = ApiUtil.successCall(user);
        when(githubService.getUser("foo")).thenReturn(call);
        Observer<Resource<User>> observer = mock(Observer.class);

        repo.loadUser("foo").observeForever(observer);
        verify(githubService, never()).getUser("foo");
        MutableLiveData<User> updatedDbData = new MutableLiveData<>();
        when(userDao.findByLogin("foo")).thenReturn(updatedDbData);
        dbData.setValue(null);
        verify(githubService).getUser("foo");
    }

    @Test
    public void dontGoToNetwork() {
        MutableLiveData<User> dbData = new MutableLiveData<>();
        User user = TestUtil.createUser("foo");
        dbData.setValue(user);
        when(userDao.findByLogin("foo")).thenReturn(dbData);
        Observer<Resource<User>> observer = mock(Observer.class);
        repo.loadUser("foo").observeForever(observer);
        verify(githubService, never()).getUser("foo");
        verify(observer).onChanged(Resource.success(user));
    }
}