/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.example.github.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.android.example.github.R
import com.android.example.github.binding.FragmentDataBindingComponent
import com.android.example.github.databinding.SearchFragmentBinding
import com.android.example.github.di.Injectable
import com.android.example.github.ui.common.NavigationController
import com.android.example.github.ui.common.RepoListAdapter
import com.android.example.github.ui.common.RetryCallback
import com.android.example.github.util.AutoClearedValue
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import javax.inject.Inject

class SearchFragment : Fragment(), Injectable {

    @field:Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @field:Inject
    lateinit var navigationController: NavigationController

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    internal lateinit var binding: AutoClearedValue<SearchFragmentBinding>

    internal lateinit var adapter: AutoClearedValue<RepoListAdapter>

    private var searchViewModel: SearchViewModel? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val dataBinding = DataBindingUtil
                .inflate<SearchFragmentBinding>(inflater, R.layout.search_fragment, container, false,
                                                dataBindingComponent)
        binding = AutoClearedValue(this, dataBinding)
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchViewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
        initRecyclerView()
        val rvAdapter = RepoListAdapter(dataBindingComponent, true, object : RepoListAdapter.RepoClickCallback {
            override fun onClick(repo: Repo) {
                navigationController.navigateToRepo(repo.owner.login, repo.name)
            }
        })
        binding.get()!!.repoList.adapter = rvAdapter
        adapter = AutoClearedValue(this, rvAdapter)

        initSearchInputListener()

        binding.get()!!.callback = object : RetryCallback {
            override fun retry() {
                searchViewModel!!.refresh()
            }
        }
    }

    private fun initSearchInputListener() {
        binding.get()!!.input.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    doSearch(v)
                    true
                }
                else -> false
            }
        }
        binding.get()!!.input.setOnKeyListener { v, keyCode, event ->
            return@setOnKeyListener when {
                event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER -> {
                    doSearch(v)
                    true
                }
                else -> false
            }
        }
    }

    private fun doSearch(v: View) {
        val query = binding.get()!!.input.text.toString()
        // Dismiss keyboard
        dismissKeyboard(v.windowToken)
        binding.get()!!.query = query
        searchViewModel!!.setQuery(query)
    }

    private fun initRecyclerView() {

        binding.get()!!.repoList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager
                        .findLastVisibleItemPosition()
                if (lastPosition == adapter.get()!!.itemCount - 1) {
                    searchViewModel!!.loadNextPage()
                }
            }
        })

        searchViewModel!!.results.observe(
                this,
                Observer { result: Resource<List<Repo>>? ->
                    binding.get()!!.searchResource = result
                    binding.get()!!.resultCount = if (result?.data == null)
                        0
                    else
                        result.data.size

                    adapter.get()!!.setList(if (result == null) null else result.data)
                    binding.get()!!.executePendingBindings()
                })

        searchViewModel!!.loadMoreStatus.observe(this, Observer { loadingMore ->
            if (loadingMore == null) {
                binding.get()!!.loadingMore = false
            } else {
                binding.get()!!.loadingMore = loadingMore.isRunning
                val error = loadingMore.errorMessageIfNotHandled
                if (error != null) {
                    Snackbar.make(binding.get()!!.loadMoreBar, error, Snackbar.LENGTH_LONG).show()
                }
            }
            binding.get()!!.executePendingBindings()
        })

    }

    private fun dismissKeyboard(windowToken: IBinder) {
        val activity = activity
        if (activity != null) {
            val imm = activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}
