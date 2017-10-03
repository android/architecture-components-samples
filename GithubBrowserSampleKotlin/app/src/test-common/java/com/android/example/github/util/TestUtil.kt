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

package com.android.example.github.util

import com.android.example.github.vo.Contributor
import com.android.example.github.vo.Repo
import com.android.example.github.vo.User

object TestUtil {

    fun createUser(login: String): User {
        return User(login, null, login + " name", null, null, null)
    }

    fun createRepos(count: Int, owner: String, name: String,
                    description: String): List<Repo> {
        return (0 until count).map { createRepo("$owner$it", "$name$it", "$description$it") }
    }

    fun createRepo(owner: String, name: String, description: String): Repo {
        return createRepo(Repo.UNKNOWN_ID, owner, name, description)
    }

    fun createRepo(id: Int, owner: String, name: String, description: String): Repo {
        return Repo(id, name, "$owner/$name", description, Repo.Owner(owner, null), 3)
    }

    fun createContributor(repo: Repo, login: String, contributions: Int): Contributor {
        val contributor = Contributor(login, contributions, null)
        contributor.repoName = repo.name
        contributor.repoOwner = repo.owner.login
        return contributor
    }
}
