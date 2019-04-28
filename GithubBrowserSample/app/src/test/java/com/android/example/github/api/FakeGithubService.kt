/*
 * Copyright 2019 The Android Open Source Project
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

package com.android.example.github.api

import androidx.lifecycle.LiveData
import com.android.example.github.vo.Contributor
import com.android.example.github.vo.Repo
import com.android.example.github.vo.User
import retrofit2.Call

/**
 * Fake API implementation that does not implement anything.
 * Designed for tests to fake
 */
open class FakeGithubService(
    var getUserImpl: suspend (login: String) -> ApiResponse<User> = notImplemented1()
) : GithubService {
    override suspend fun getUser(login: String) = getUserImpl(login)

    override fun getRepos(login: String): LiveData<ApiResponse<List<Repo>>> {
        TODO("not implemented")
    }

    override fun getRepo(owner: String, name: String): LiveData<ApiResponse<Repo>> {
        TODO("not implemented")
    }

    override fun getContributors(
        owner: String,
        name: String
    ): LiveData<ApiResponse<List<Contributor>>> {
        TODO("not implemented")
    }

    override fun searchRepos(query: String): LiveData<ApiResponse<RepoSearchResponse>> {
        TODO("not implemented")
    }

    override fun searchRepos(query: String, page: Int): Call<RepoSearchResponse> {
        TODO("not implemented")
    }

    companion object {
        private fun <T, R> notImplemented1(): suspend (t: T) -> R {
            return { t: T ->
                TODO("")
            }
        }

        private fun <T, R> notImplemented2(): suspend (t: T) -> R {
            return { t: T ->
                TODO("")
            }
        }
    }
}

