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

package com.android.example.github.ui.user

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.example.github.AppExecutors
import com.android.example.github.R
import com.android.example.github.binding.FragmentDataBindingComponent
import com.android.example.github.databinding.UserFragmentBinding
import com.android.example.github.di.Injectable
import com.android.example.github.testing.OpenForTesting
import com.android.example.github.ui.common.RepoListAdapter
import com.android.example.github.ui.common.RetryCallback
import com.android.example.github.util.autoCleared
import javax.inject.Inject

@OpenForTesting
class UserFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<UserFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private lateinit var userViewModel: UserViewModel
    private var adapter by autoCleared<RepoListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<UserFragmentBinding>(
            inflater,
            R.layout.user_fragment,
            container,
            false,
            dataBindingComponent
        )
        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                userViewModel.retry()
            }
        }
        binding = dataBinding
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(UserViewModel::class.java)
        val params = UserFragmentArgs.fromBundle(arguments)
        userViewModel.setLogin(params.login)

        userViewModel.user.observe(this, Observer { userResource ->
            binding.user = userResource?.data
            binding.userResource = userResource
        })
        val rvAdapter = RepoListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            showFullName = false
        ) { repo ->
            navController().navigate(UserFragmentDirections.showRepo(repo.owner.login, repo.name))
        }
        binding.repoList.adapter = rvAdapter
        this.adapter = rvAdapter
        initRepoList()
    }

    private fun initRepoList() {
        userViewModel.repositories.observe(this, Observer { repos ->
            adapter.submitList(repos?.data)
        })
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
