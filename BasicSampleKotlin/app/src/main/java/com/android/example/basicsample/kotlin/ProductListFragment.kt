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

package com.android.example.basicsample.kotlin

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.example.basicsample.kotlin.databinding.ListFragmentBinding
import com.android.example.basicsample.kotlin.model.Product
import com.android.example.basicsample.kotlin.ui.ProductAdapter
import com.android.example.basicsample.kotlin.ui.ProductClickCallback
import com.android.example.basicsample.kotlin.viewmodel.ProductListViewModel

class ProductListFragment : Fragment() {

    private var mProductAdapter: ProductAdapter? = null

    private var mBinding: ListFragmentBinding? = null

    private val mProductClickCallback = object : ProductClickCallback {
        override fun onClick(product: Product) {

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                (activity as MainActivity).show(product)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater!!, R.layout.list_fragment, container, false)

        mProductAdapter = ProductAdapter(mProductClickCallback)
        mBinding!!.productsList.adapter = mProductAdapter

        return mBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(ProductListViewModel::class.java)

        subscribeUi(viewModel)
    }

    private fun subscribeUi(viewModel: ProductListViewModel) {
        // Update the list when the data changes
        viewModel.products.observe(this, Observer { myProducts ->
            if (myProducts != null) {
                mBinding!!.isLoading = false
                mProductAdapter!!.setProductList(myProducts)
            } else {
                mBinding!!.isLoading = true
            }
        })
    }

    companion object {

        val TAG = "ProductListViewModel"
    }
}
