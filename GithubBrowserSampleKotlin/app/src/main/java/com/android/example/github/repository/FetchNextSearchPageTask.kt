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

import android.arch.lifecycle.MutableLiveData
import com.android.example.github.api.ApiResponse
import com.android.example.github.api.GithubService
import com.android.example.github.db.GithubDb
import com.android.example.github.vo.RepoSearchResult
import com.android.example.github.vo.Resource
import java.io.IOException
import java.util.*

/**
 * A task that reads the search result in the database and fetches the next page, if it has one.
 */
class FetchNextSearchPageTask internal constructor(private val query: String, private val githubService: GithubService, private val db: GithubDb) : Runnable {
    val liveData = MutableLiveData<Resource<Boolean>>()

    override fun run() {
        val current = db.repoDao().findSearchResult(query)
        if (current == null) {
            liveData.postValue(null)
            return
        }
        val nextPage = current.next
        if (nextPage == null) {
            liveData.postValue(Resource.success(false))
            return
        }
        try {
            val response = githubService
                    .searchRepos(query, nextPage).execute()
            val apiResponse = ApiResponse(response)
            if (apiResponse.isSuccessful) {
                // we merge all repo ids into 1 list so that it is easier to fetch the result list.
                val ids = ArrayList<Int>()
                ids.addAll(current.repoIds)

                ids.addAll(apiResponse.body!!.repoIds)
                val merged = RepoSearchResult(query, ids,
                                              apiResponse.body.total, apiResponse.nextPage)
                try {
                    db.beginTransaction()
                    db.repoDao().insert(merged)
                    db.repoDao().insertRepos(apiResponse.body.items!!)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                liveData.postValue(Resource.success(apiResponse.nextPage != null))
            } else {
                liveData.postValue(Resource.error(apiResponse.errorMessage, true))
            }
        } catch (e: IOException) {
            liveData.postValue(Resource.error(e.message, true))
        }

    }


}
