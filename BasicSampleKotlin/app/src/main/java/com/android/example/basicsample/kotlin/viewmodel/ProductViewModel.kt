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

package com.android.example.basicsample.kotlin.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.databinding.ObservableField
import com.android.example.basicsample.kotlin.db.DatabaseCreator
import com.android.example.basicsample.kotlin.db.entity.CommentEntity
import com.android.example.basicsample.kotlin.db.entity.ProductEntity

class ProductViewModel(application: Application,
                       private val mProductId: Int) : AndroidViewModel(application) {

    val observableProduct: LiveData<ProductEntity>

    var product = ObservableField<ProductEntity>()

    /**
     * Expose the LiveData Comments query so the UI can observe it.
     */
    val comments: LiveData<List<CommentEntity>>

    init {
        ABSENT.value = null
        ABSENT2.value = null
    }

    init {

        val databaseCreator = DatabaseCreator.getInstance(this.getApplication())

        comments = Transformations.switchMap(databaseCreator.isDatabaseCreated) { isDbCreated ->
            when {
                isDbCreated -> databaseCreator.database!!.commentDao().loadComments(mProductId)
                else -> ABSENT
            }
        }

        observableProduct = Transformations.switchMap(databaseCreator.isDatabaseCreated) { isDbCreated ->
            when {
                isDbCreated -> databaseCreator.database!!.productDao().loadProduct(mProductId)
                else -> ABSENT2
            }
        }

        databaseCreator.createDb(this.getApplication())

    }

    fun setProduct(product: ProductEntity) {
        this.product.set(product)
    }

    /**
     * A creator is used to inject the product ID into the ViewModel
     *
     *
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    class Factory(private val mApplication: Application, private val mProductId: Int) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return ProductViewModel(mApplication, mProductId) as T
        }
    }

    companion object {
        private val ABSENT = MutableLiveData<List<CommentEntity>>()
        private val ABSENT2 = MutableLiveData<ProductEntity>()

    }
}
