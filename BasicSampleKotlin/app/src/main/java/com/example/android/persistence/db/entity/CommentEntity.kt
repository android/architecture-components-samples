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

package com.example.android.persistence.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.example.android.persistence.model.Comment
import java.util.*

@Entity(tableName = "comments",
    foreignKeys = [ForeignKey(entity = ProductEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("productId"),
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["productId"])])
class CommentEntity : Comment {
    @PrimaryKey(autoGenerate = true)
    override var id: Int = 0
    override var productId: Int = 0
    override var text: String? = null
    override var postedAt: Date? = null

    constructor() {}

    constructor(id: Int, productId: Int, text: String, postedAt: Date) {
        this.id = id
        this.productId = productId
        this.text = text
        this.postedAt = postedAt
    }
}
