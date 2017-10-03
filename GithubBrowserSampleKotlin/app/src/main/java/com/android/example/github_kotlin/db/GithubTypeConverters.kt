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

package com.android.example.github_kotlin.db

import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.util.StringUtil

object GithubTypeConverters {
    @JvmStatic
    @TypeConverter
    fun stringToIntList(data: String?): List<Int>? {
        return if (data == null) {
            emptyList()
        } else StringUtil.splitToIntList(data)
    }

    @JvmStatic
    @TypeConverter
    fun intListToString(ints: List<Int>): String? {
        return StringUtil.joinIntoString(ints)
    }
}
