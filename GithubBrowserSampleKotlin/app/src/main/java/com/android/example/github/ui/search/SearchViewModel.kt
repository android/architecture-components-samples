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

import android.arch.lifecycle.*
import android.arch.lifecycle.Observer
import android.support.annotation.VisibleForTesting
import com.android.example.github.OpenClassOnDebug
import com.android.example.github.repository.RepoRepository
import com.android.example.github.util.AbsentLiveData
import com.android.example.github.util.Objects
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.Status
import java.util.*
import javax.inject.Inject

@OpenClassOnDebug
class SearchViewModel
@Inject
internal constructor(repoRepository: RepoRepository) : ViewModel() {

    private val query = MutableLiveData<String>()
    val results: LiveData<Resource<List<Repo>>>

    private val nextPageHandler: NextPageHandler
    val loadMoreStatus: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    init {
        nextPageHandler = NextPageHandler(repoRepository)
        results = Transformations.switchMap(query) {
            if (it == null || it.trim { it <= ' ' }.isEmpty()) {
                AbsentLiveData.create()
            } else {
                repoRepository.search(it)
            }
        }
    }

    fun setQuery(originalInput: String) {
        val input = originalInput.toLowerCase(Locale.getDefault()).trim { it <= ' ' }
        if (Objects.equals(input, query.value)) {
            return
        }
        nextPageHandler.reset()
        query.value = input
    }

    fun loadNextPage() {
        val value = query.value ?: return
        if (value.trim { it <= ' ' }.isEmpty()) {
            return
        }
        nextPageHandler.queryNextPage(value)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun refresh() {
        if (query.value != null) {
            query.value = query.value
        }
    }

    class LoadMoreState(val isRunning: Boolean, val errorMessage: String?) {
        private var handledError = false

        val errorMessageIfNotHandled: String?
            get() {
                if (handledError) {
                    return null
                }
                handledError = true
                return errorMessage
            }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        class NextPageHandler
        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        constructor(private val repository: RepoRepository) : Observer<Resource<Boolean>> {
            private var nextPageLiveData: LiveData<Resource<Boolean>>? = null
            val loadMoreState = MutableLiveData<LoadMoreState>()
            private var query: String? = null
            @VisibleForTesting
            var hasMore: Boolean = false

            init {
                reset()
            }

            fun queryNextPage(query: String) {
                if (Objects.equals(this.query, query)) {
                    return
                }
                unregister()
                this.query = query
                nextPageLiveData = repository.searchNextPage(query)
                loadMoreState.value = LoadMoreState(true, null)

                nextPageLiveData!!.observeForever(this)
            }

            override fun onChanged(result: Resource<Boolean>?) {
                if (result == null) {
                    reset()
                } else {
                    when (result.status) {
                        Status.SUCCESS -> {
                            hasMore = java.lang.Boolean.TRUE == result.data
                            unregister()
                            loadMoreState.setValue(LoadMoreState(false, null))
                        }
                        Status.ERROR -> {
                            hasMore = true
                            unregister()
                            loadMoreState.setValue(LoadMoreState(false,
                                                                 result.message))
                        }
                        Status.LOADING -> {

                        }
                    }
                }
            }

            private fun unregister() {
                if (nextPageLiveData != null) {
                    nextPageLiveData!!.removeObserver(this)
                    nextPageLiveData = null
                    if (hasMore) {
                        query = null
                    }
                }
            }

            fun reset() {
                unregister()
                hasMore = true
                loadMoreState.value = LoadMoreState(false, null)
            }
        }
    }
}
