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

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The Room database that contains the Users table
 */
@Database(entities = {User.class}, version = 1)
public abstract class UsersDatabase extends RoomDatabase {

    private static volatile UsersDatabase INSTANCE;

    public static UsersDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (UsersDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UsersDatabase.class, "Sample.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();

}
