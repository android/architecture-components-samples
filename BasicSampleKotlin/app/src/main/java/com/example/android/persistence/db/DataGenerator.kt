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

package com.example.android.persistence.db

import com.example.android.persistence.db.entity.CommentEntity
import com.example.android.persistence.db.entity.ProductEntity

import java.util.ArrayList
import java.util.Date
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * Generates data to pre-populate the database
 */
object DataGenerator {
    private val FIRST = arrayOf("Special edition", "New", "Cheap", "Quality", "Used")
    private val SECOND = arrayOf("Three-headed Monkey", "Rubber Chicken", "Pint of Grog", "Monocle")
    private val DESCRIPTION = arrayOf("is finally here", "is recommended by Stan S. Stanman", "is the best sold product on Mêlée Island", "is \uD83D\uDCAF", "is ❤️", "is fine")
    private val COMMENTS = arrayOf("Comment 1", "Comment 2", "Comment 3", "Comment 4", "Comment 5", "Comment 6")

    fun generateProducts(): List<ProductEntity> = ArrayList<ProductEntity>(FIRST.size * SECOND.size).also { products ->
        val random = Random()

        for (i in FIRST.indices) {
            for (j in SECOND.indices) {
                products.add(ProductEntity().apply {
                    name = FIRST[i] + " " + SECOND[j]
                    description = name + " " + DESCRIPTION[j]
                    price = random.nextInt(240)
                    id = FIRST.size * i + j + 1
                })
            }
        }
    }

    fun generateCommentsForProducts(products: List<ProductEntity>): List<CommentEntity> = ArrayList<CommentEntity>().also { comments ->
        val random = Random()

        for (product in products) {
            val commentsNumber = random.nextInt(5) + 1
            for (i in 0 until commentsNumber) {
                comments.add(CommentEntity().apply {
                    productId = product.id
                    text = COMMENTS[i] + " for " + product.name
                    postedAt = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis((commentsNumber - i).toLong()) + TimeUnit.HOURS.toMillis(i.toLong()))
                })
            }
        }
    }
}
