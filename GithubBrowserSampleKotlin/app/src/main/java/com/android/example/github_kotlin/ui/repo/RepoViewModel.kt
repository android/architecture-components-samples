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

package com.android.example.github_kotlin.ui.repo

import android.arch.lifecycle.*
import android.arch.paging.PagedList
import android.support.annotation.VisibleForTesting
import com.android.example.github_kotlin.OpenClassOnDebug
import com.android.example.github_kotlin.repository.RepoRepository
import com.android.example.github_kotlin.util.AbsentLiveData
import com.android.example.github_kotlin.util.Objects
import com.android.example.github_kotlin.vo.Contributor
import com.android.example.github_kotlin.vo.Repo
import com.android.example.github_kotlin.vo.Resource
import javax.inject.Inject

@OpenClassOnDebug
class RepoViewModel
@Inject
constructor(repository: RepoRepository) : ViewModel() {
    private val _repoId: MutableLiveData<RepoId> = MutableLiveData()
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    val repoId: MutableLiveData<RepoId>
        get() = _repoId

    val repo: LiveData<Resource<Repo>>
    val contributors: LiveData<Resource<PagedList<Contributor>>>

    init {
        repo = Transformations.switchMap(repoId) { input ->
            when {
                input.isEmpty -> AbsentLiveData.create()
                else -> repository.loadRepo(input.owner, input.name)
            }
        }
        contributors = Transformations.switchMap(repoId) { input ->
            if (input.isEmpty || input.owner == null || input.name == null) {
                AbsentLiveData.create()
            } else {
                repository.loadContributors(input.owner, input.name)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun retry() {
        val current: RepoId? = repoId.value
        if (current != null && !current.isEmpty) {
            repoId.value = current
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun setId(owner: String?, name: String?) {
        val update = RepoId(owner, name)
        if (Objects.equals(repoId.value, update)) {
            return
        }
        repoId.value = update
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    class RepoId(owner: String?, name: String?) {
        val owner: String? = owner?.trim { it <= ' ' }
        val name: String? = name?.trim { it <= ' ' }

        val isEmpty: Boolean
            get() = owner == null || name == null || owner.isEmpty() || name.isEmpty()

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }

            val repoId = other as RepoId?

            if (if (owner != null) owner != repoId!!.owner else repoId!!.owner != null) {
                return false
            }
            return if (name != null) name == repoId.name else repoId.name == null
        }

        override fun hashCode(): Int {
            var result = owner?.hashCode() ?: 0
            result = 31 * result + (name?.hashCode() ?: 0)
            return result
        }
    }
}
