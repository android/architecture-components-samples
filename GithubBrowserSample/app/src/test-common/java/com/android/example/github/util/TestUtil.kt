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

    fun createUser(login: String) = User(
        login = login,
        avatarUrl = null,
        name = "$login name",
        company = null,
        reposUrl = null,
        blog = null
    )

    fun createRepos(count: Int, owner: String, name: String, description: String): List<Repo> {
        return (0 until count).map {
            createRepo(
                owner = owner + it,
                name = name + it,
                description = description + it
            )
        }
    }

    fun createRepo(owner: String, name: String, description: String) = createRepo(
        id = Repo.UNKNOWN_ID,
        owner = owner,
        name = name,
        description = description
    )

    fun createRepo(id: Int, owner: String, name: String, description: String) = Repo(
        id = id,
        name = name,
        fullName = "$owner/$name",
        description = description,
        owner = Repo.Owner(owner, null),
        stars = 3
    )

    fun createContributor(repo: Repo, login: String, contributions: Int) = Contributor(
        login = login,
        contributions = contributions,
        avatarUrl = null
    ).apply {
        repoName = repo.name
        repoOwner = repo.owner.login
    }
}
