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

package com.android.example.github.ui.common

import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.support.v7.recyclerview.extensions.DiffCallback
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.example.github.R
import com.android.example.github.databinding.RepoItemBinding
import com.android.example.github.vo.Repo

/**
 * A RecyclerView adapter for [Repo] class.
 */
class RepoListAdapter(private val dataBindingComponent: DataBindingComponent,
                      private val showFullName: Boolean,
                      private val repoClickCallback: RepoClickCallback) : DataBoundListAdapter<Repo, RepoItemBinding>(object : DiffCallback<Repo>() {
    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem.owner == newItem.owner && oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem == newItem
    }

}) {

    override fun createBinding(parent: ViewGroup): RepoItemBinding {
        val binding = DataBindingUtil
                .inflate<RepoItemBinding>(LayoutInflater.from(parent.context), R.layout.repo_item,
                                          parent, false, dataBindingComponent)
        binding.showFullName = showFullName
        binding.root.setOnClickListener { _ ->
            val repo = binding.repo
            if (repo != null) {
                repoClickCallback.onClick(repo)
            }
        }
        return binding
    }

    override fun bind(binding: RepoItemBinding, item: Repo) {
        binding.repo = item
    }

    interface RepoClickCallback {
        fun onClick(repo: Repo)
    }
}
