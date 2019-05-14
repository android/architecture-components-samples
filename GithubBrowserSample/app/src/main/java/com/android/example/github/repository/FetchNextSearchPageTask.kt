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

package com.android.example.github.repository

import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.android.example.github.api.ApiEmptyResponse
import com.android.example.github.api.ApiErrorResponse
import com.android.example.github.api.ApiSuccessResponse
import com.android.example.github.api.GithubService
import com.android.example.github.db.GithubDb
import com.android.example.github.vo.RepoSearchResult
import com.android.example.github.vo.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.IOException

/**
 * A task that reads the search result in the database and fetches the next page, if it has one.
 */
fun fetchNextSearch(
    query: String,
    githubService: GithubService,
    db: GithubDb,
    dispatcher : CoroutineDispatcher
) = liveData(dispatcher) {
    val current = db.repoDao().findSearchResult(query)
    if (current == null) {
        emit(null)
        return@liveData
    }
    val nextPage = current.next
    if (nextPage == null) {
        emit(Resource.success(false))
        return@liveData
    }
    val newValue = try {
        val apiResponse = githubService.searchRepos(query, nextPage)
        when (apiResponse) {
            is ApiSuccessResponse -> {
                // we merge all repo ids into 1 list so that it is easier to fetch the
                // result list.
                val ids = arrayListOf<Int>()
                ids.addAll(current.repoIds)

                ids.addAll(apiResponse.body.items.map { it.id })
                val merged = RepoSearchResult(
                    query, ids,
                    apiResponse.body.total, apiResponse.nextPage
                )
                db.withTransaction {
                    db.repoDao().insert(merged)
                    db.repoDao().insertRepos(apiResponse.body.items)
                }
                Resource.success(apiResponse.nextPage != null)
            }
            is ApiEmptyResponse -> {
                Resource.success(false)
            }
            is ApiErrorResponse -> {
                Resource.error(apiResponse.errorMessage, true)
            }
        }

    } catch (e: IOException) {
        Resource.error(e.message!!, true)
    }
    emit(newValue)
}
