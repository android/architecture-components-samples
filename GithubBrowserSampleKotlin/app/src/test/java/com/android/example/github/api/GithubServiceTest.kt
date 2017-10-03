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

package com.android.example.github.api

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.example.github.util.LiveDataCallAdapterFactory
import com.android.example.github.util.LiveDataTestUtil.getValue
import com.android.example.github.vo.Repo
import com.android.example.github.vo.User
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNull.notNullValue
import org.junit.*
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@RunWith(JUnit4::class)
class GithubServiceTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var service: GithubService? = null

    private var mockWebServer: MockWebServer? = null

    @Before
    @Throws(IOException::class)
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer!!.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(GithubService::class.java)
    }

    @After
    @Throws(IOException::class)
    fun stopService() {
        mockWebServer!!.shutdown()
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun getUser() {
        enqueueResponse("user-yigit.json")
        val yigit = getValue(service!!.getUser("yigit")).body

        val request = mockWebServer!!.takeRequest()
        assertThat(request.path, `is`("/users/yigit"))

        assertThat<User>(yigit, notNullValue())
        assertThat<String>(yigit!!.avatarUrl, `is`("https://avatars3.githubusercontent.com/u/89202?v=3"))
        assertThat<String>(yigit.company, `is`("Google"))
        assertThat<String>(yigit.blog, `is`("birbit.com"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun getRepos() {
        enqueueResponse("repos-yigit.json")
        val repos = getValue(service!!.getRepos("yigit")).body

        val request = mockWebServer!!.takeRequest()
        assertThat(request.path, `is`("/users/yigit/repos"))

        assert(repos != null)
        assertThat(repos!!.size, `is`(2))

        val (_, _, fullName, _, owner) = repos[0]
        assertThat(fullName, `is`("yigit/AckMate"))

        assertThat<Repo.Owner>(owner, notNullValue())
        assertThat(owner.login, `is`("yigit"))
        assertThat<String>(owner.url, `is`("https://api.github.com/users/yigit"))

        val (_, _, fullName1) = repos[1]
        assertThat(fullName1, `is`("yigit/android-architecture"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun getContributors() {
        enqueueResponse("contributors.json")
        val contributors = getValue(
                service!!.getContributors("foo", "bar")).body!!
        assertThat(contributors.size, `is`(3))
        val yigit = contributors[0]
        assertThat(yigit.login, `is`("yigit"))
        assertThat<String>(yigit.avatarUrl, `is`("https://avatars3.githubusercontent.com/u/89202?v=3"))
        assertThat(yigit.contributions, `is`(291))
        assertThat(contributors[1].login, `is`("guavabot"))
        assertThat(contributors[2].login, `is`("coltin"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun search() {
        val header = "<https://api.github.com/search/repositories?q=foo&page=2>; rel=\"next\", " +
                "<https://api.github.com/search/repositories?q=foo&page=34>; rel=\"last\""
        val headers = HashMap<String, String>()
        headers.put("link", header)
        enqueueResponse("search.json", headers)
        val response = getValue(
                service!!.searchRepos("foo"))

        assertThat(response, notNullValue())
        assertThat(response.body!!.total, `is`(41))
        assertThat(response.body!!.items!!.size, `is`(30))
        assertThat<String>(response.links["next"],
                           `is`("https://api.github.com/search/repositories?q=foo&page=2"))
        assertThat<Int>(response.nextPage, `is`(2))
    }

    @Throws(IOException::class)
    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader
                .getResourceAsStream("api-response/" + fileName)
        val source = Okio.buffer(Okio.source(inputStream))
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer!!.enqueue(mockResponse
                                        .setBody(source.readString(StandardCharsets.UTF_8)))
    }
}
