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
import static org.junit.Assert.assertNull;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link LocalUserDataSource}.
 */
@RunWith(AndroidJUnit4.class)
public class LocalUserDataSourceTest {

    private LocalUserDataSource mDataSource;

    private static final User USER = new User(1, "username");

    @Before
    public void initDb() throws Exception {
        mDataSource = LocalUserDataSource.getInstance(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void cleanUp() {
        mDataSource.deleteAllUsers();
    }

    @Test
    public void insertAndGetUser() {
        // When inserting a new user in the data source
        mDataSource.insertOrUpdateUser(USER);

        //The user can be retrieved
        User dbUser = mDataSource.getUser();
        assertEquals(dbUser.getId(), USER.getId());
        assertEquals(dbUser.getUserName(), USER.getUserName());
    }

    @Test
    public void updateAndGetUser() {
        // Given that we have a user in the data source
        mDataSource.insertOrUpdateUser(USER);

        // When we are updating the name of the user
        User updatedUser = new User(USER.getId(), "new username");
        mDataSource.insertOrUpdateUser(updatedUser);

        //The retrieved user has the updated username
        User dbUser = mDataSource.getUser();
        assertEquals(dbUser.getId(), USER.getId());
        assertEquals(dbUser.getUserName(), "new username");
    }

    @Test
    public void deleteAndGetUser() {
        // Given that we have a user in the data source
        mDataSource.insertOrUpdateUser(USER);

        //When we are deleting all users
        mDataSource.deleteAllUsers();

        // The user is no longer in the data source
        User dbUser = mDataSource.getUser();
        assertNull(dbUser);
    }
}