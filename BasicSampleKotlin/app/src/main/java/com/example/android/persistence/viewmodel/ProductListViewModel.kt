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
package com.example.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer

import com.example.android.persistence.BasicApp
import com.example.android.persistence.db.entity.ProductEntity

class ProductListViewModel(application: Application) : AndroidViewModel(application) {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private val observableProducts = MediatorLiveData<List<ProductEntity>>()

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    val products: LiveData<List<ProductEntity>>
        get() = observableProducts

    init {
        // set by default null, until we get data from the database.
        observableProducts.value = null

        val products = (application as BasicApp).repository.products

        // observe the changes of the products from the database and forward them
        @Suppress("NAME_SHADOWING")
        observableProducts.addSource(products) { products -> observableProducts.setValue(products) }
    }
}
