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

package com.android.example.github_kotlin.ui.user

import android.arch.lifecycle.*
import android.support.annotation.VisibleForTesting
import com.android.example.github_kotlin.OpenClassOnDebug
import com.android.example.github_kotlin.repository.RepoRepository
import com.android.example.github_kotlin.repository.UserRepository
import com.android.example.github_kotlin.util.AbsentLiveData
import com.android.example.github_kotlin.util.Objects
import com.android.example.github_kotlin.vo.Repo
import com.android.example.github_kotlin.vo.Resource
import com.android.example.github_kotlin.vo.User
import javax.inject.Inject

@OpenClassOnDebug
class UserViewModel
@Inject
constructor(userRepository: UserRepository, repoRepository: RepoRepository) : ViewModel() {
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    val login = MutableLiveData<String>()
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    val repositories: LiveData<Resource<List<Repo>>>
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    val user: LiveData<Resource<User>>

    init {
        user = Transformations.switchMap(login) { login ->
            if (login == null) {
                AbsentLiveData.create()
            } else {
                userRepository.loadUser(login)
            }
        }
        repositories = Transformations.switchMap(login) { login ->
            if (login == null) {
                AbsentLiveData.create<Resource<List<Repo>>>()
            } else {
                repoRepository.loadRepos(login)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun setLogin(login: String?) {
        if (Objects.equals(this.login.value, login)) {
            return
        }
        this.login.value = login
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    fun retry() {
        if (this.login.value != null) {
            this.login.value = this.login.value
        }
    }
}
