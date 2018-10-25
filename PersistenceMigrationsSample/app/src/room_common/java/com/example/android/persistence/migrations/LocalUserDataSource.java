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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

/**
 * Concrete implementation of the {@link LocalUserDataSource} that works with Room.
 */
public class LocalUserDataSource implements UserDataSource {

    private static volatile LocalUserDataSource INSTANCE;

    private UserDao mUserDao;

    @VisibleForTesting
    LocalUserDataSource(UserDao userDao) {
        mUserDao = userDao;
    }

    public static LocalUserDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (LocalUserDataSource.class) {
                if (INSTANCE == null) {
                    UsersDatabase database = UsersDatabase.getInstance(context);
                    INSTANCE = new LocalUserDataSource(database.userDao());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public User getUser() {
        return mUserDao.getUser();
    }

    @Override
    public void insertOrUpdateUser(User user) {
        mUserDao.insertUser(user);
    }

    @Override
    public void deleteAllUsers() {
        mUserDao.deleteAllUsers();
    }
}
