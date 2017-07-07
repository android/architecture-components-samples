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

package com.example.android.persistence.migrations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the UserRepository.
 */
public class UserRepositoryTest {

    @Mock
    private UserDataSource mUserDataSource;

    private UserRepository mUserRepository;

    private static final User USER = new User("username");

    @Captor
    private ArgumentCaptor<User> mUserArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mUserRepository = new UserRepository(new SingleExecutors(), mUserDataSource);
    }

    @Test
    public void getUserWithUserInDataSource() throws Exception {
        //Given a callback for loading the user
        LoadUserCallback callback = mock(LoadUserCallback.class);
        // And user in the data source
        when(mUserDataSource.getUser()).thenReturn(USER);

        // When requesting a user from the repository
        mUserRepository.getUser(callback);

        // The user is requested from the user data source
        verify(mUserDataSource).getUser();
        // and the callback triggers correct method
        verify(callback).onUserLoaded(USER);
    }

    @Test
    public void getUserWithNoUserInDataSource() throws Exception {
        //Given a callback for loading the user
        LoadUserCallback callback = mock(LoadUserCallback.class);
        // And no user in the data source
        when(mUserDataSource.getUser()).thenReturn(null);

        // When requesting a user from the repository
        mUserRepository.getUser(callback);

        // The user is requested from the user data source
        verify(mUserDataSource).getUser();
        // and the callback triggers correct method
        verify(callback).onDataNotAvailable();
    }

    @Test
    public void updateUserName() throws Exception {
        // Given a callback for updating the username
        UpdateUserCallback callback = mock(UpdateUserCallback.class);

        // When updating the username
        mUserRepository.updateUserName("name", callback);

        // The user with the correct user name was saved
        verify(mUserDataSource).insertOrUpdateUser(mUserArgumentCaptor.capture());
        User user = mUserArgumentCaptor.getValue();
        assertEquals("name", user.getUserName());
        // The callback is triggered
        verify(callback).onUserUpdated(user);
    }

}