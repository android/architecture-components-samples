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

package com.android.example.paging.pagingwithnetwork.reddit.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost

/**
 * Database schema used by the DbRedditPostRepository
 */
@Database(
        entities = arrayOf(RedditPost::class),
        version = 1,
        exportSchema = false
)
abstract class RedditDb : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory : Boolean): RedditDb {
            val databaseBuilder = if(useInMemory) {
                Room.inMemoryDatabaseBuilder(context, RedditDb::class.java)
            } else {
                Room.databaseBuilder(context, RedditDb::class.java, "reddit.db")
            }
            return databaseBuilder
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }

    abstract fun posts(): RedditPostDao
}