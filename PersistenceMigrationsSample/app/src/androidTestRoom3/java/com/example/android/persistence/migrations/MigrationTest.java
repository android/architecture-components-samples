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
import static com.example.android.persistence.migrations.UsersDatabase.MIGRATION_1_4;
import static com.example.android.persistence.migrations.UsersDatabase.MIGRATION_2_3;
import static com.example.android.persistence.migrations.UsersDatabase.MIGRATION_3_4;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.room.Room;
import androidx.room.testing.MigrationTestHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Test the migration from different database schema versions to version 4.
 */
@RunWith(AndroidJUnit4.class)
public class MigrationTest {

    private static final String TEST_DB_NAME = "test-db";

    private static final User USER = new User("id", "username",
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
        mSqliteTestDbHelper = new SqliteTestDbOpenHelper(ApplicationProvider.getApplicationContext()(),
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
    public void migrationFrom1To4_containsCorrectData() throws IOException {
        // Create the database with the initial version 1 schema and insert a user
        SqliteDatabaseTestHelper.insertUser(1, USER.getUserName(), mSqliteTestDbHelper);

        // Re-open the database with version 4 and provide
        // MIGRATION_2_3 and MIGRATION_3_4 as the migration process.
        mMigrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 4, true,
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4);
        // MigrationTestHelper automatically verifies the schema changes, but not the data validity
        // Validate that the data was migrated properly.
        User dbUser = getMigratedRoomDatabase().userDao().getUser();
        assertEquals(dbUser.getId(), "1");
        assertEquals(dbUser.getUserName(), USER.getUserName());
    }

    @Test
    public void migrationFrom2To4_containsCorrectData() throws IOException {
        // Create the database with version 2
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB_NAME, 2);
        // Insert some data
        insertUser(1, USER.getUserName(), db);
        //Prepare for the next version
        db.close();

        // Re-open the database with version 4 and provide
        // MIGRATION_2_3 and MIGRATION_3_4 as the migration process.
        mMigrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 4, true,
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4);

        // MigrationTestHelper automatically verifies the schema changes, but not the data validity
        // Validate that the data was migrated properly.
        User dbUser = getMigratedRoomDatabase().userDao().getUser();
        assertEquals(dbUser.getId(), "1");
        assertEquals(dbUser.getUserName(), USER.getUserName());
        // The date was missing in version 2, so it should be null in version 3
        assertEquals(dbUser.getDate(), null);
    }


    @Test
    public void migrationFrom3To4_containsCorrectData() throws IOException {
        // Create the database with version 3
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB_NAME, 3);
        // db has schema version 3. Insert some data
        insertUser(1, USER.getUserName(), DateConverter.toTimestamp(USER.getDate()), db);
        //Prepare for the next version
        db.close();

        // Re-open the database with version 4 and provide
        // MIGRATION_2_3 and MIGRATION_3_4 as the migration process.
        mMigrationTestHelper.runMigrationsAndValidate(TEST_DB_NAME, 4, true,
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4);

        // MigrationTestHelper automatically verifies the schema changes, but not the data validity
        // Validate that the data was migrated properly.
        User dbUser = getMigratedRoomDatabase().userDao().getUser();
        assertEquals(dbUser.getId(), "1");
        assertEquals(dbUser.getUserName(), USER.getUserName());
        assertEquals(dbUser.getDate(), USER.getDate());
    }

    @Test
    public void startInVersion4_containsCorrectData() throws IOException {
        // Create the database with version 4
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB_NAME, 4);
        // insert some data
        insertUser(USER.getId(), USER.getUserName(), DateConverter.toTimestamp(USER.getDate()), db);
        db.close();

        // Get the latest, migrated, version of the database
        UsersDatabase usersDatabase = getMigratedRoomDatabase();

        // verify that the data is correct
        User dbUser = getMigratedRoomDatabase().userDao().getUser();
        assertEquals(dbUser.getId(), USER.getId());
        assertEquals(dbUser.getUserName(), USER.getUserName());
        assertEquals(dbUser.getDate(), USER.getDate());
    }

    private UsersDatabase getMigratedRoomDatabase() {
        UsersDatabase database = Room.databaseBuilder(ApplicationProvider.getApplicationContext()(),
                UsersDatabase.class, TEST_DB_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_1_4)
                .build();
        // close the database and release any stream resources when the test finishes
        mMigrationTestHelper.closeWhenFinished(database);
        return database;
    }

    private void insertUser(int id, String userName, SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("userid", id);
        values.put("username", userName);

        db.insert("users", SQLiteDatabase.CONFLICT_REPLACE, values);
    }

    private void insertUser(int id, String userName, long date, SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("userid", id);
        values.put("username", userName);
        values.put("last_update", date);

        db.insert("users", SQLiteDatabase.CONFLICT_REPLACE, values);
    }

    private void insertUser(String id, String userName, long date, SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("userid", id);
        values.put("username", userName);
        values.put("last_update", date);

        db.insert("users", SQLiteDatabase.CONFLICT_REPLACE, values);
    }
}
