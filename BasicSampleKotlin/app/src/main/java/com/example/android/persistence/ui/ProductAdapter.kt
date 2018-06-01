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

package com.example.android.persistence.ui

import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.android.persistence.R
import com.example.android.persistence.databinding.ProductItemBinding
import com.example.android.persistence.model.Product

class ProductAdapter(
    private val productClickCallback: ProductClickCallback?
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private var productList: List<Product>? = null

    fun setProductList(newProducts: List<Product>) {
        val productList = productList
        if (productList == null) {
            this@ProductAdapter.productList = newProducts
            notifyItemRangeInserted(0, newProducts.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return productList.size
                }

                override fun getNewListSize(): Int {
                    return newProducts.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return productList[oldItemPosition].id == newProducts[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val newProduct = newProducts[newItemPosition]
                    val oldProduct = productList[oldItemPosition]
                    return (newProduct.id == oldProduct.id
                        && newProduct.description == oldProduct.description
                        && newProduct.name == oldProduct.name
                        && newProduct.price == oldProduct.price)
                }
            })
            this@ProductAdapter.productList = newProducts
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder =
        ProductViewHolder(
            DataBindingUtil
                .inflate<ProductItemBinding>(LayoutInflater.from(parent.context), R.layout.product_item,
                    parent, false).also { binding ->
                    binding.callback = productClickCallback
                }
        )


    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList!![position])
    }

    override fun getItemCount(): Int = productList?.size ?: 0

    inner class ProductViewHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()
        }
    }
}
