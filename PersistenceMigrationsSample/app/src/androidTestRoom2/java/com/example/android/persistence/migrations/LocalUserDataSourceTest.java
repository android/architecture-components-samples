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

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Integration tests for the {@link LocalUserDataSource} implementation with Room.
 */
public class LocalUserDataSourceTest {

    private static final User USER = new User(1, "username", new Date());

    private UsersDatabase mDatabase;
    private LocalUserDataSource mDataSource;

    @Before
    public void initDb() throws Exception {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext()(),
                UsersDatabase.class).build();
        mDataSource = new LocalUserDataSource(mDatabase.userDao());
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void insertAndGetUser() {
        // When inserting a new user in the data source
        mDataSource.insertOrUpdateUser(USER);

        //The user can be retrieved
        User dbUser = mDataSource.getUser();
        assertEquals(dbUser.getId(), USER.getId());
        assertEquals(dbUser.getUserName(), USER.getUserName());
        assertEquals(dbUser.getDate(), USER.getDate());
    }

    @Test
    public void updateAndGetUser() {
        // Given that we have a user in the data source
        mDataSource.insertOrUpdateUser(USER);

        // When we are updating the name of the user
        User updatedUser = new User(USER.getId(), "new username", USER.getDate());
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
