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

@file:Suppress("FunctionName")

package com.android.example.github_kotlin.db

import android.database.sqlite.SQLiteException
import android.support.test.runner.AndroidJUnit4
import com.android.example.github_kotlin.util.LiveDataTestUtil.getValue
import com.android.example.github_kotlin.util.TestUtil
import com.android.example.github_kotlin.vo.Repo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class RepoDaoTest : DbTest() {
    @Test
    @Throws(InterruptedException::class)
    fun insertAndRead() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        db.repoDao().insert(repo)
        val loaded = getValue(db.repoDao().load("foo", "bar"))
        assertThat(loaded, notNullValue())
        assertThat(loaded.name, `is`("bar"))
        assertThat<String>(loaded.description, `is`("desc"))
        assertThat<Repo.Owner>(loaded.owner, notNullValue())
        assertThat(loaded.owner.login, `is`("foo"))
    }

    @Test
    fun insertContributorsWithoutRepo() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val contributor = TestUtil.createContributor(repo, "c1", 3)
        try {
            db.repoDao().insertContributors(listOf(contributor))
            throw AssertionError("must fail because repo does not exist")
        } catch (ignored: SQLiteException) {
        }

    }

    @Test
    @Throws(InterruptedException::class)
    fun insertContributors() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val c1 = TestUtil.createContributor(repo, "c1", 3)
        val c2 = TestUtil.createContributor(repo, "c2", 7)
        db.beginTransaction()
        try {
            db.repoDao().insert(repo)
            db.repoDao().insertContributors(Arrays.asList(c1, c2))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        val provider = db.repoDao().loadContributors("foo", "bar")
        val list = getValue(provider.create(0, 10))
        assertThat(list.size, `is`(2))
        val first = list[0]!!

        assertThat(first.login, `is`("c2"))
        assertThat(first.contributions, `is`(7))

        val second = list[1]!!
        assertThat(second.login, `is`("c1"))
        assertThat(second.contributions, `is`(3))
    }

    @Test
    @Throws(InterruptedException::class)
    fun createIfNotExists_exists() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        db.repoDao().insert(repo)
        assertThat(db.repoDao().createRepoIfNotExists(repo), `is`(-1L))
    }

    @Test
    fun createIfNotExists_doesNotExist() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        assertThat(db.repoDao().createRepoIfNotExists(repo), `is`(1L))
    }

    @Test
    @Throws(InterruptedException::class)
    fun insertContributorsThenUpdateRepo() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        db.repoDao().insert(repo)
        val contributor = TestUtil.createContributor(repo, "aa", 3)
        db.repoDao().insertContributors(listOf(contributor))

        val provider = db.repoDao().loadContributors("foo", "bar")
        val data = provider.create(null, 10)
        assertThat(getValue(data).size, `is`(1))

        val update = TestUtil.createRepo("foo", "bar", "desc")
        db.repoDao().insert(update)
        assertThat(getValue(data).size, `is`(1))
    }
}
