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
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField

import com.example.android.persistence.BasicApp
import com.example.android.persistence.DataRepository
import com.example.android.persistence.db.entity.ProductEntity

class ProductViewModel(
    application: Application,
    repository: DataRepository,
    productId: Int
) : AndroidViewModel(application) {
    val observableProduct = repository.loadProduct(productId)

    var product = ObservableField<ProductEntity>()

    /**
     * Expose the LiveData Comments query so the UI can observe it.
     */
    val comments = repository.loadComments(productId)

    fun setProduct(product: ProductEntity) {
        this.product.set(product)
    }

    /**
     * A creator is used to inject the product ID into the ViewModel
     *
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    class Factory(
        private val application: Application,
        private val productId: Int
    ) : ViewModelProvider.NewInstanceFactory() {
        private val repository = (application as BasicApp).repository

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductViewModel(application, repository, productId) as T
    }
}
