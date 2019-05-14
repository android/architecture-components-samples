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

package com.android.example.github.db

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import androidx.test.runner.AndroidJUnit4
import com.android.example.github.util.TestUtil
import com.android.example.github.util.await
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepoDaoTest : DbTest() {
    @Test
    fun insertAndRead() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val loaded = runBlocking {
            db.repoDao().insert(repo)
            db.repoDao().load("foo", "bar").await()!!
        }
        assertThat(loaded, notNullValue())
        assertThat(loaded.name, `is`("bar"))
        assertThat(loaded.description, `is`("desc"))
        assertThat(loaded.owner, notNullValue())
        assertThat(loaded.owner.login, `is`("foo"))
    }

    @Test
    fun insertContributorsWithoutRepo() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val contributor = TestUtil.createContributor(repo, "c1", 3)
        runBlocking {
            try {
                db.repoDao().insertContributors(listOf(contributor))
                throw AssertionError("must fail because repo does not exist")
            } catch (ex: SQLiteException) {
            }
        }
    }

    @Test
    fun insertContributors() {
        runBlocking {
            val repo = TestUtil.createRepo("foo", "bar", "desc")
            val c1 = TestUtil.createContributor(repo, "c1", 3)
            val c2 = TestUtil.createContributor(repo, "c2", 7)
            db.withTransaction {
                db.repoDao().insert(repo)
                db.repoDao().insertContributors(arrayListOf(c1, c2))
            }
            val list = db.repoDao().loadContributors("foo", "bar").await()!!
            assertThat(list.size, `is`(2))
            val first = list[0]

            assertThat(first.login, `is`("c2"))
            assertThat(first.contributions, `is`(7))

            val second = list[1]
            assertThat(second.login, `is`("c1"))
            assertThat(second.contributions, `is`(3))
        }
    }

    @Test
    fun createIfNotExists_exists() {
        runBlocking {
            val repo = TestUtil.createRepo("foo", "bar", "desc")
            db.repoDao().insert(repo)
            assertThat(db.repoDao().createRepoIfNotExists(repo), `is`(-1L))
        }
    }

    @Test
    fun createIfNotExists_doesNotExist() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        assertThat(db.repoDao().createRepoIfNotExists(repo), `is`(1L))
    }

    @Test
    fun insertContributorsThenUpdateRepo() {
        runBlocking {
            val repo = TestUtil.createRepo("foo", "bar", "desc")
            db.repoDao().insert(repo)
            val contributor = TestUtil.createContributor(repo, "aa", 3)
            db.repoDao().insertContributors(listOf(contributor))
            var data = db.repoDao().loadContributors("foo", "bar")
            assertThat(data.await()!!.size, `is`(1))

            val update = TestUtil.createRepo("foo", "bar", "desc")
            db.repoDao().insert(update)
            data = db.repoDao().loadContributors("foo", "bar")
            assertThat(data.await()!!.size, `is`(1))
        }
    }
}
