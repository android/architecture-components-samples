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

import static com.example.android.persistence.migrations.UsersDatabase.MIGRATION_1_2;
import static com.example.android.persistence.migrations.UsersDatabase.MIGRATION_2_3;

import static org.junit.Assert.assertEquals;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.room.Room;
import androidx.room.testing.MigrationTestHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

/**
 * Test the migration from versions 1 and 2, to version 3.
 */
@RunWith(AndroidJUnit4.class)
public class MigrationTest {

    private static final String TEST_DB_NAME = "test-db";

    private static final User USER = new User(1, "username",
            new Date(System.currentTimeMillis()));

    // Helper for creating Room databases and migrations
    @Rule
    public MigrationTestHelper mMigrationTestHelper =
            new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                    UsersDatabase.class.getCanonicalName(),
                    new FrameworkSQLiteOpenHelperFactory());

    // Helper for creating SQLite database in version 1
    private SqliteTestDbOpenHelper mSqliteTestDbHelper;

    @Before
    public void setUp() throws Exception {
        // To test migrations from version 1 of the database, we need to create the database
        // with version 1 using SQLite API
        mSqliteTestDbHelper = new SqliteTestDbOpenHelper(InstrumentationRegistry.getTargetContext(),
                TEST_DB_NAME);
        // We're creating the table for every test, to ensure that the table is in the correct state
        SqliteDatabaseTestHelper.createTable(mSqliteTestDbHelper);
    }

    @After
    public void tearDown() throws Exception {
        // Clear the database after every test
        SqliteDatabaseTestHelper.clearDatabase(mSqliteTestDbHelper);
    }

    @Test
    public void migrationFrom1To3_containsCorrectData() throws IOException {
        // Create the database with the initial version 1 schema and insert a user
        SqliteDatabaseTestHelper.insertUser(1, USER.getUserName(), mSqliteTestDbHelper);

        // Re-open the database with version 3 and provide MIGRATION_1_2 and
        // MIGRATION_2_3 as the migration process.
        mMigrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 3, true,
                MIGRATION_1_2, MIGRATION_2_3);

        // Get the latest, migrated, version of the database
        // Check that the correct data is in the database
        User dbUser = getMigratedRoomDatabase().userDao().getUser();
        assertEquals(dbUser.getId(), 1);
        assertEquals(dbUser.getUserName(), USER.getUserName());
        // The date was missing in version 2, so it should be null in version 3
        assertEquals(dbUser.getDate(), null);
    }

    @Test
    public void migrationFrom2To3_containsCorrectData() throws IOException {
        // Create the database in version 2
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB_NAME, 2);
        // Insert some data
        insertUser(USER.getId(), USER.getUserName(), db);
        //Prepare for the next version
        db.close();

        // Re-open the database with version 3 and provide MIGRATION_1_2 and
        // MIGRATION_2_3 as the migration process.
        mMigrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 3, true,
                MIGRATION_1_2, MIGRATION_2_3);

        // MigrationTestHelper automatically verifies the schema changes, but not the data validity
        // Validate that the data was migrated properly.
        User dbUser = getMigratedRoomDatabase().userDao().getUser();
        assertEquals(dbUser.getId(), USER.getId());
        assertEquals(dbUser.getUserName(), USER.getUserName());
        // The date was missing in version 2, so it should be null in version 3
        assertEquals(dbUser.getDate(), null);
    }

    @Test
    public void startInVersion3_containsCorrectData() throws IOException {
        // Create the database with version 3
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB_NAME, 3);
        // insert some data
        insertUser(USER, db);
        db.close();

        // open the db with Room.
        UsersDatabase usersDatabase = getMigratedRoomDatabase();

        // verify that the data is correct
        User dbUser = usersDatabase.userDao().getUser();
        assertEquals(dbUser.getId(), USER.getId());
        assertEquals(dbUser.getUserName(), USER.getUserName());
        assertEquals(dbUser.getDate(), USER.getDate());
    }

    private UsersDatabase getMigratedRoomDatabase() {
        UsersDatabase database = Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
                UsersDatabase.class, TEST_DB_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build();
        // close the database and release any stream resources when the test finishes
        mMigrationTestHelper.closeWhenFinished(database);
        return database;
    }

    private void insertUser(int id, String username, SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("userid", id);
        values.put("username", username);

        db.insert("users", SQLiteDatabase.CONFLICT_REPLACE, values);
    }

    private void insertUser(User user, SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("userid", user.getId());
        values.put("username", user.getUserName());
        values.put("last_update", DateConverter.toTimestamp(user.getDate()));

        db.insert("users", SQLiteDatabase.CONFLICT_REPLACE, values);
    }
}
