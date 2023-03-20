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

package com.android.example.paging.pagingwithnetwork.reddit.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "posts",
        indices = [Index(value = ["subreddit"], unique = false)])
data class RedditPost(
        // A unique Int key starting at 0 which autoIncrements by 1 with every insert.
        // We need this immutable key to maintain consistent item ordering so that the UI
        // does not scroll out of order when new posts are appended and inserted into this db.
        @PrimaryKey(autoGenerate = true)
        val _id: Int?,
        @SerializedName("name")
        val name: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("score")
        val score: Int,
        @SerializedName("author")
        val author: String,
        @SerializedName("subreddit") // technically mutable but fine for a demo
        @ColumnInfo(collate = ColumnInfo.NOCASE)
        val subreddit: String,
        @SerializedName("num_comments")
        val num_comments: Int,
        @SerializedName("created_utc")
        val created: Long,
        val thumbnail: String?,
        val url: String?) {
}