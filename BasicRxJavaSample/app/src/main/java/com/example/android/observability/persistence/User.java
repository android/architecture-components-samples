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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Immutable model class for a User
 */
@Entity(tableName = "users")
public class User {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "userid")
    private final String mId;

    @NonNull
    @ColumnInfo(name = "username")
    private final String mUserName;

    @Ignore// forbid room from using this constructor
    public User(@NonNull String userName) {
        mId = UUID.randomUUID().toString();
        mUserName = userName;
    }

    public User(@NonNull String id, @NonNull String userName) {
        this.mId = id;
        this.mUserName = userName;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getUserName() {
        return mUserName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!mId.equals(user.mId)) return false;
        return mUserName.equals(user.mUserName);
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mUserName.hashCode();
        return result;
    }
}
