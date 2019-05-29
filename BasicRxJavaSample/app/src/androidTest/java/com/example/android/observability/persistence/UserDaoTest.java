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

package com.example.android.observability.persistence;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the implementation of {@link UserDao}
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class UserDaoTest {

    private static final User USER = new User("id", "username");
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private UsersDatabase mDatabase;
    private UserDao dao;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        Context context = ApplicationProvider.getApplicationContext();
        mDatabase = Room.inMemoryDatabaseBuilder(context, UsersDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();
        dao = mDatabase.userDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void getUsersWhenNoUserInserted() {
        dao.getUser()
                .test()
                .assertNoValues();
    }

    @Test
    public void insertAndGetUser() {
        // When inserting a new user in the data source
        dao.insertUser(USER).blockingAwait();

        // When subscribing to the emissions of the user
        dao.getUser()
                .test()
                // assertValue asserts that there was only one emission of the user
                .assertValue(USER);
    }

    @Test
    public void updateAndGetUser() {
        // Given that we have a user in the data source
        dao.insertUser(USER).blockingAwait();

        // When we are updating the name of the user
        User updatedUser = new User(USER.getId(), "new username");
        dao.insertUser(updatedUser).blockingAwait();

        // When subscribing to the emissions of the user
        dao.getUser()
                .test()
                // assertValue asserts that there was only one emission of the user
                .assertValue(user -> {
                    // The emitted user is the expected one
                    return user != null && user.getId().equals(USER.getId()) &&
                            user.getUserName().equals("new username");
                });
    }

    @Test
    public void deleteAndGetUser() {
        // Given that we have a user in the data source
        dao.insertUser(USER).blockingAwait();

        //When we are deleting all users
        dao.deleteAllUsers();
        // When subscribing to the emissions of the user
        dao.getUser()
                .test()
                // check that there's no user emitted
                .assertNoValues();
    }
}
