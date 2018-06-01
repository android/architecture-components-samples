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
import android.arch.persistence.room.PrimaryKey

import com.example.android.persistence.model.Product

@Entity(tableName = "products")
class ProductEntity : Product {
    @PrimaryKey
    override var id: Int = 0
    override var name: String? = null
    override var description: String? = null
    override var price: Int = 0

    constructor() {}

    constructor(id: Int, name: String, description: String, price: Int) {
        this.id = id
        this.name = name
        this.description = description
        this.price = price
    }

    constructor(product: Product) {
        this.id = product.id
        this.name = product.name
        this.description = product.description
        this.price = product.price
    }
}
