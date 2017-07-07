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

/**
 * Access point for accessing user data.
 */
public interface UserDataSource {

    /**
     * Gets all the users from the data source.
     *
     * @return all the users from the data source.
     */
    User getUser();

    /**
     * Inserts the user in the data source, or, if this is an existing user, it updates it.
     *
     * @param user the user to be inserted or updated.
     */
    void insertOrUpdateUser(User user);

    /**
     * Deletes all users from the data source.
     */
    void deleteAllUsers();
}
