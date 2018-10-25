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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.runner.AndroidJUnit4
import com.android.example.github.util.LiveDataTestUtil.getValue
import com.android.example.github.util.TestUtil
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest : DbTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertAndLoad() {
        val user = TestUtil.createUser("foo")
        db.userDao().insert(user)

        val loaded = getValue(db.userDao().findByLogin(user.login))
        assertThat(loaded.login, `is`("foo"))

        val replacement = TestUtil.createUser("foo2")
        db.userDao().insert(replacement)

        val loadedReplacement = getValue(db.userDao().findByLogin(replacement.login))
        assertThat(loadedReplacement.login, `is`("foo2"))
    }
}
