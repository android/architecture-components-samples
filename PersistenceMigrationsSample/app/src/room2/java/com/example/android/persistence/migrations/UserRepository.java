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

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * The repository is responsible of handling user data operations.
 */
public class UserRepository {

    private AppExecutors mAppExecutors;

    private UserDataSource mUserDataSource;

    private User mCachedUser;

    public UserRepository(AppExecutors appExecutors, UserDataSource userDataSource) {
        mAppExecutors = appExecutors;
        mUserDataSource = userDataSource;
    }

    /**
     * Get the user from the data source, cache it and notify via the callback that the user has
     * been retrieved.
     *
     * @param callback callback that gets called when the user was retrieved from the data source.
     */
    void getUser(final LoadUserCallback callback) {
        final WeakReference<LoadUserCallback> loadUserCallback = new WeakReference<>(callback);

        // request the user on the I/O thread
        mAppExecutors.diskIO().execute(() -> {
            final User user = mUserDataSource.getUser();
            // notify on the main thread
            mAppExecutors.mainThread().execute(() -> {
                final LoadUserCallback userCallback = loadUserCallback.get();
                if (userCallback == null) {
                    return;
                }
                if (user == null) {
                    userCallback.onDataNotAvailable();
                } else {
                    mCachedUser = user;
                    userCallback.onUserLoaded(mCachedUser);
                }
            });
        });
    }

    /**
     * Insert an new user or update the name of the user.
     *
     * @param userName the user name
     * @param callback callback that gets triggered when the user was updated.
     */
    void updateUserName(String userName, UpdateUserCallback callback) {
        final WeakReference<UpdateUserCallback> updateUserCallback = new WeakReference<>(callback);

        Date date = new Date(System.currentTimeMillis());
        final User user = mCachedUser == null
                ? new User(userName)
                : new User(mCachedUser.getId(), userName, date);

        // update the user on the I/O thread
        mAppExecutors.diskIO().execute(() -> {
            mUserDataSource.insertOrUpdateUser(user);
            mCachedUser = user;
            // notify on the main thread
            mAppExecutors.mainThread().execute(() -> {
                UpdateUserCallback userCallback = updateUserCallback.get();
                if (userCallback != null) {
                    userCallback.onUserUpdated(user);
                }
            });
        });
    }
}
