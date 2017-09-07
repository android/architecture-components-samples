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

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the implementation of {@link UserDao}
 */
@RunWith(AndroidJUnit4.class)
public class UserDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final User USER = new User("id", "username");

    private UsersDatabase mDatabase;

    @Before
    public void initDb() throws Exception {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                UsersDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void getUsersWhenNoUserInserted() {
        mDatabase.userDao().getUser()
                .test()
                .assertNoValues();
    }

    @Test
    public void insertAndGetUser() {
        // When inserting a new user in the data source
        mDatabase.userDao().insertUser(USER);

        // When subscribing to the emissions of the user
        mDatabase.userDao().getUser()
                .test()
                // assertValue asserts that there was only one emission of the user
                .assertValue(user -> {
                    // The emitted user is the expected one
                    return user != null && user.getId().equals(USER.getId()) &&
                            user.getUserName().equals(USER.getUserName());
                });
    }

    @Test
    public void updateAndGetUser() {
        // Given that we have a user in the data source
        mDatabase.userDao().insertUser(USER);

        // When we are updating the name of the user
        User updatedUser = new User(USER.getId(), "new username");
        mDatabase.userDao().insertUser(updatedUser);

        // When subscribing to the emissions of the user
        mDatabase.userDao().getUser()
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
        mDatabase.userDao().insertUser(USER);

        //When we are deleting all users
        mDatabase.userDao().deleteAllUsers();
        // When subscribing to the emissions of the user
        mDatabase.userDao().getUser()
                .test()
                // check that there's no user emitted
                .assertNoValues();
    }
}
