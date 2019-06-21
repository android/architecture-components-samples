package com.example.android.persistence.migrations;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Helper class for working with the SQLiteDatabase.
 */
public class SqliteDatabaseTestHelper {

    public static void insertUser(int userid, String userName, SqliteTestDbOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userid", userid);
        values.put("username", userName);

        db.insertWithOnConflict("users", null, values,
                SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public static void createTable(SqliteTestDbOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS users (userid INTEGER PRIMARY KEY NOT NULL,"
                + " username TEXT )");

        db.close();
    }

    public static void clearDatabase(SqliteTestDbOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS users");

        db.close();
    }
}
