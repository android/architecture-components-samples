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

package com.android.example.github_kotlin.repository

import android.arch.lifecycle.LiveData
import com.android.example.github_kotlin.AppExecutors
import com.android.example.github_kotlin.api.ApiResponse
import com.android.example.github_kotlin.api.GithubService
import com.android.example.github_kotlin.db.UserDao
import com.android.example.github_kotlin.vo.Resource
import com.android.example.github_kotlin.vo.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles User objects.
 */
@Singleton
class UserRepository
@Inject
internal constructor(
        private val appExecutors: AppExecutors,
        private val userDao: UserDao,
        private val githubService: GithubService) {

    fun loadUser(login: String): LiveData<Resource<User>> {
        return object : NetworkBoundResource<User, User>(appExecutors) {
            override fun saveCallResult(item: User) {
                userDao.insert(item)
            }

            override fun shouldFetch(data: User?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<User> {
                return userDao.findByLogin(login)
            }

            override fun createCall(): LiveData<ApiResponse<User>> {
                return githubService.getUser(login)
            }
        }.asLiveData()
    }
}
