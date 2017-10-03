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

package com.android.example.basicsample.kotlin.ui

import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.example.basicsample.kotlin.R
import com.android.example.basicsample.kotlin.databinding.ProductItemBinding
import com.android.example.basicsample.kotlin.model.Product

class ProductAdapter(private val mProductClickCallback: ProductClickCallback?) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    internal var mProductList: List<Product>? = null

    fun setProductList(productList: List<Product>) {
        if (mProductList == null) {
            mProductList = productList
            notifyItemRangeInserted(0, productList.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mProductList!!.size
                }

                override fun getNewListSize(): Int {
                    return productList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return mProductList!![oldItemPosition].id == productList[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val newProduct = productList[newItemPosition]
                    val oldProduct = mProductList!![oldItemPosition]
                    return (newProduct.id == oldProduct.id
                            && newProduct.description == oldProduct.description
                            && newProduct.name == oldProduct.name
                            && newProduct.price == oldProduct.price)
                }
            })
            mProductList = productList
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = DataBindingUtil
                .inflate<ProductItemBinding>(LayoutInflater.from(parent.context), R.layout.product_item,
                                             parent, false)
        binding.callback = mProductClickCallback
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.binding.product = mProductList!![position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return if (mProductList == null) 0 else mProductList!!.size
    }

    class ProductViewHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root)
}
