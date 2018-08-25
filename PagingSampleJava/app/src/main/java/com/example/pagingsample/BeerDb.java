/*
 *
 *  * Copyright (C) 2018 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.example.pagingsample;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Database(entities = {Beer.class}, version = 1)
public abstract class BeerDb extends RoomDatabase {

    private static final String BEER_DATABASE_NAME = "beer_db";

    public abstract BeerDao beerDao();

    private static BeerDb INSTANCE;

    public static BeerDb getInstance(Context context, final Executor diskIo) {
        if (INSTANCE == null) {
            synchronized (BeerDb.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        BeerDb.class, BEER_DATABASE_NAME).addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        diskIo.execute(() -> {
                            insertBeer(BeerDb.getInstance(context, diskIo), getBeers());
                        });
                    }
                }).build();
            }
        }
        return INSTANCE;
    }

    private static void insertBeer(final BeerDb beerDb, final List<Beer> beers) {
        beerDb.runInTransaction(() -> beerDb.beerDao().insertAll(beers));
    }

    private static List<Beer> getBeers() {
        List<Beer> beers = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Beer beer = new Beer();
            beer.setName("Beer " + i);
            beer.setCountryOfOrigin("Country of origin " + i);
            beers.add(beer);
        }
        return beers;
    }
}
