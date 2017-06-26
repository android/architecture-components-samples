package com.android.example.github.db;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;


/**
 * Created by kanusornpakd on 6/26/2017 AD.
 */

public class MigrationDb {
    public MigrationDb(Context context) {
        Room.databaseBuilder(context, GithubDb.class, "database-name")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
    }


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, "
                    + "`name` TEXT, PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Book "
                    + " ADD COLUMN pub_year INTEGER");
        }
    };
}
