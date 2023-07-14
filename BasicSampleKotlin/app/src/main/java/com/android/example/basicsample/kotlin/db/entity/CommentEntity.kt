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

package com.android.example.basicsample.kotlin.db.entity

import android.arch.persistence.room.*
import com.android.example.basicsample.kotlin.model.Comment
import java.util.*

@Entity(tableName = "comments",
        foreignKeys = arrayOf(ForeignKey(entity = ProductEntity::class,
                                         parentColumns = arrayOf("id"),
                                         childColumns = arrayOf("productId"),
                                         onDelete = ForeignKey.CASCADE)),
        indices = arrayOf(Index(value = "productId")))
data class CommentEntity(
        @PrimaryKey(autoGenerate = true)
        override val id: Int = 0,
        override val productId: Int,
        override val text: String,
        override val postedAt: Date) : Comment {

    constructor(comment: Comment) : this(0,
                                         comment.productId,
                                         comment.text,
                                         comment.postedAt)
}
