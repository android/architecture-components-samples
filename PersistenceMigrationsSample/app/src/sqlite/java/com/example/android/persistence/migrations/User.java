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

import java.util.Random;

/**
 * Model for a user that is saved in the SQLite database and used in the UI.
 */
public class User {

    private int mId;

    private String mUserName;

    public User(String userName) {
        // DO NOT USE Integer Random values for primary keys.
        // This is using an Integer to showcase a WRONG implementation that has to be fixed
        // afterwards by updating the schema.
        // The ID is updated to a UUID String in the room3 flavor.
        mId = new Random(Integer.MAX_VALUE).nextInt();
        mUserName = userName;
    }

    public User(int id, String userName) {
        this.mId = id;
        this.mUserName = userName;
    }

    public int getId() {
        return mId;
    }

    public String getUserName() {
        return mUserName;
    }
}
