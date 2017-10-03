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

package com.android.example.github_kotlin.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListProvider
import android.arch.persistence.room.*
import android.util.SparseIntArray
import com.android.example.github_kotlin.vo.Contributor
import com.android.example.github_kotlin.vo.Repo
import com.android.example.github_kotlin.vo.RepoSearchResult
import java.util.*

/**
 * Interface for database access on Repo related operations.
 */
@Dao
abstract class RepoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg repos: Repo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertContributors(contributors: List<Contributor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertRepos(repositories: List<Repo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun createRepoIfNotExists(repo: Repo): Long

    @Query("SELECT * FROM repo WHERE owner_login = :login AND name = :name")
    abstract fun load(login: String?, name: String?): LiveData<Repo>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT login, avatarUrl, contributions FROM contributor "
                   + "WHERE repoName = :name AND repoOwner = :owner "
                   + "ORDER BY contributions DESC")
    abstract fun loadContributors(owner: String, name: String): LivePagedListProvider<Int, Contributor>

    @Query("SELECT * FROM Repo "
                   + "WHERE owner_login = :owner "
                   + "ORDER BY stars DESC")
    abstract fun loadRepositories(owner: String): LiveData<List<Repo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(result: RepoSearchResult)

    @Query("SELECT * FROM RepoSearchResult WHERE query = :query")
    abstract fun search(query: String): LiveData<RepoSearchResult>

    fun loadOrdered(repoIds: List<Int>): LiveData<List<Repo>> {
        val order = SparseIntArray()
        for ((index, repoId) in repoIds.withIndex()) {
            order.put(repoId, index)
        }
        return Transformations.map(loadById(repoIds)) { repositories ->
            Collections.sort(repositories) { r1, r2 ->
                val pos1 = order.get(r1.id)
                val pos2 = order.get(r2.id)
                pos1 - pos2
            }
            repositories
        }
    }

    @Query("SELECT * FROM Repo WHERE id in (:repoIds)")
    protected abstract fun loadById(repoIds: List<Int>): LiveData<List<Repo>>

    @Query("SELECT * FROM RepoSearchResult WHERE query = :query")
    abstract fun findSearchResult(query: String): RepoSearchResult?
}
