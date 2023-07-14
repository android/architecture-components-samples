/*
 * Copyright 2017, The Android Open Source Project
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

package com.android.example.basicsample.kotlin.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

import com.android.example.basicsample.kotlin.db.dao.CommentDao
import com.android.example.basicsample.kotlin.db.dao.ProductDao
import com.android.example.basicsample.kotlin.db.entity.CommentEntity
import com.android.example.basicsample.kotlin.db.entity.ProductEntity
import com.android.example.basicsample.kotlin.db.converter.DateConverter

@Database(entities = arrayOf(ProductEntity::class, CommentEntity::class), version = 1)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    abstract fun commentDao(): CommentDao

    companion object {

        internal val DATABASE_NAME = "basic-sample-db"
    }
}
