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

import static com.example.android.persistence.migrations.UserPersistenceContract.UserEntry
        .COLUMN_NAME_ENTRY_ID;
import static com.example.android.persistence.migrations.UserPersistenceContract.UserEntry
        .COLUMN_NAME_USERNAME;
import static com.example.android.persistence.migrations.UserPersistenceContract.UserEntry
        .TABLE_NAME;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UsersDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Sample.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ENTRY_ID + " INTEGER  PRIMARY KEY NOT NULL," +
                    COLUMN_NAME_USERNAME + " TEXT )";

    public UsersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
