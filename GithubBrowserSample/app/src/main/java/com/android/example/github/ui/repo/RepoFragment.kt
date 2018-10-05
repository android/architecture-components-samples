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

package com.android.example.github.ui.repo

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.example.github.AppExecutors
import com.android.example.github.R
import com.android.example.github.binding.FragmentDataBindingComponent
import com.android.example.github.databinding.RepoFragmentBinding
import com.android.example.github.di.Injectable
import com.android.example.github.testing.OpenForTesting
import com.android.example.github.ui.common.RetryCallback
import com.android.example.github.util.autoCleared
import javax.inject.Inject

/**
 * The UI Controller for displaying a Github Repo's information with its contributors.
 */
@OpenForTesting
class RepoFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var repoViewModel: RepoViewModel

    @Inject
    lateinit var appExecutors: AppExecutors

    // mutable for testing
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<RepoFragmentBinding>()

    private var adapter by autoCleared<ContributorAdapter>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        repoViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RepoViewModel::class.java)
        val params = RepoFragmentArgs.fromBundle(arguments)
        repoViewModel.setId(params.owner, params.name)

        val repo = repoViewModel.repo
        repo.observe(this, Observer { resource ->
            binding.repo = resource?.data
            binding.repoResource = resource
        })

        val adapter = ContributorAdapter(dataBindingComponent, appExecutors) { contributor ->
            navController().navigate(
                    RepoFragmentDirections.showUser(contributor.login)
            )
        }
        this.adapter = adapter
        binding.contributorList.adapter = adapter
        initContributorList(repoViewModel)
    }

    private fun initContributorList(viewModel: RepoViewModel) {
        viewModel.contributors.observe(this, Observer { listResource ->
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource?.data != null) {
                adapter.submitList(listResource.data)
            } else {
                adapter.submitList(emptyList())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<RepoFragmentBinding>(
            inflater,
            R.layout.repo_fragment,
            container,
            false
        )
        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                repoViewModel.retry()
            }
        }
        binding = dataBinding
        return dataBinding.root
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
