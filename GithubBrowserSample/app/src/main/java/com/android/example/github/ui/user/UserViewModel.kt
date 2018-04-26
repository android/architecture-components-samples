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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.android.example.github.repository.RepoRepository
import com.android.example.github.repository.UserRepository
import com.android.example.github.testing.OpenForTesting
import com.android.example.github.util.AbsentLiveData
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.User
import javax.inject.Inject

@OpenForTesting
class UserViewModel
@Inject constructor(userRepository: UserRepository, repoRepository: RepoRepository) : ViewModel() {
    private val _login = MutableLiveData<String>()
    val login: LiveData<String>
        get() = _login
    val repositories: LiveData<Resource<List<Repo>>> = Transformations
        .switchMap(_login) { login ->
            if (login == null) {
                AbsentLiveData.create()
            } else {
                repoRepository.loadRepos(login)
            }
        }
    val user: LiveData<Resource<User>> = Transformations
        .switchMap(_login) { login ->
            if (login == null) {
                AbsentLiveData.create()
            } else {
                userRepository.loadUser(login)
            }
        }

    fun setLogin(login: String?) {
        if (_login.value != login) {
            _login.value = login
        }
    }

    fun retry() {
        _login.value?.let {
            _login.value = it
        }
    }
}
