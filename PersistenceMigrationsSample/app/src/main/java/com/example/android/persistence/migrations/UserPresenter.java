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

import androidx.annotation.Nullable;

/**
 * Listens for users's actions from the UI {@link UserActivity}, retrieves the data and updates
 * the UI as required.
 */
public class UserPresenter {

    private UserRepository mDataSource;

    @Nullable
    private UserView mView;

    private LoadUserCallback mLoadUserCallback;
    private UpdateUserCallback mUpdateUserCallback;

    public UserPresenter(UserRepository dataSource, UserView view) {
        mDataSource = dataSource;
        mView = view;

        mLoadUserCallback = createLoadUserCallback();
        mUpdateUserCallback = createUpdateUserCallback();
    }

    /**
     * Start working with the view.
     */
    public void start() {
        mDataSource.getUser(mLoadUserCallback);
    }

    public void stop() {
        mView = null;
    }

    /**
     * Update the username of the user.
     *
     * @param userName the new userName
     */
    public void updateUserName(final String userName) {

        mDataSource.updateUserName(userName, mUpdateUserCallback);
    }

    private LoadUserCallback createLoadUserCallback() {
        return new LoadUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (mView != null) {
                    mView.showUserName(user.getUserName());
                }
            }

            @Override
            public void onDataNotAvailable() {
                if (mView != null) {
                    mView.hideUserName();
                }
            }
        };
    }

    private UpdateUserCallback createUpdateUserCallback() {
        return user -> {
            if (mView != null) {
                mView.showUserName(user.getUserName());
            }
        };
    }
}
