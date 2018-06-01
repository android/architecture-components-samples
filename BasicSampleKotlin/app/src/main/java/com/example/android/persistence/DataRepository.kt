package com.example.android.persistence

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

import com.example.android.persistence.db.AppDatabase
import com.example.android.persistence.db.entity.CommentEntity
import com.example.android.persistence.db.entity.ProductEntity

/**
 * Repository handling the work with products and comments.
 */
class DataRepository private constructor(
    private val database: AppDatabase
) {
    private val observableProducts = MediatorLiveData<List<ProductEntity>>()

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    val products: LiveData<List<ProductEntity>>
        get() = observableProducts

    init {
        observableProducts.addSource(database.productDao().loadAllProducts()) { productEntities ->
            if (database.databaseCreated.value != null) {
                observableProducts.postValue(productEntities)
            }
        }
    }

    fun loadProduct(productId: Int): LiveData<ProductEntity> =
        database.productDao().loadProduct(productId)

    fun loadComments(productId: Int): LiveData<List<CommentEntity>> =
        database.commentDao().loadComments(productId)

    companion object {
        private var instance: DataRepository? = null

        @JvmStatic
        fun getInstance(database: AppDatabase): DataRepository {
            if (instance == null) {
                synchronized(DataRepository::class.java) {
                    if (instance == null) {
                        instance = DataRepository(database)
                    }
                }
            }
            return instance!!
        }
    }
}
