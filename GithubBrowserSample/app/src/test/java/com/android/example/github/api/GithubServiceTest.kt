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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.example.github.util.LiveDataCallAdapterFactory
import com.android.example.github.util.LiveDataTestUtil.getValue
import com.android.example.github.vo.User
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNull.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class GithubServiceTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: GithubService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(GithubService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun getUser() {
        enqueueResponse("user-yigit.json")
        val yigit = (getValue(service.getUser("yigit")) as ApiSuccessResponse).body

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/users/yigit"))

        assertThat<User>(yigit, notNullValue())
        assertThat(yigit.avatarUrl, `is`("https://avatars3.githubusercontent.com/u/89202?v=3"))
        assertThat(yigit.company, `is`("Google"))
        assertThat(yigit.blog, `is`("birbit.com"))
    }

    @Test
    fun getRepos() {
        enqueueResponse("repos-yigit.json")
        val repos = (getValue(service.getRepos("yigit")) as ApiSuccessResponse).body

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/users/yigit/repos"))

        assertThat(repos.size, `is`(2))

        val repo = repos[0]
        assertThat(repo.fullName, `is`("yigit/AckMate"))

        val owner = repo.owner
        assertThat(owner, notNullValue())
        assertThat(owner.login, `is`("yigit"))
        assertThat(owner.url, `is`("https://api.github.com/users/yigit"))

        val repo2 = repos[1]
        assertThat(repo2.fullName, `is`("yigit/android-architecture"))
    }

    @Test
    fun getContributors() {
        enqueueResponse("contributors.json")
        val value = getValue(service.getContributors("foo", "bar"))
        val contributors = (value as ApiSuccessResponse).body
        assertThat(contributors.size, `is`(3))
        val yigit = contributors[0]
        assertThat(yigit.login, `is`("yigit"))
        assertThat(yigit.avatarUrl, `is`("https://avatars3.githubusercontent.com/u/89202?v=3"))
        assertThat(yigit.contributions, `is`(291))
        assertThat(contributors[1].login, `is`("guavabot"))
        assertThat(contributors[2].login, `is`("coltin"))
    }

    @Test
    fun search() {
        val next = """<https://api.github.com/search/repositories?q=foo&page=2>; rel="next""""
        val last = """<https://api.github.com/search/repositories?q=foo&page=34>; rel="last""""
        enqueueResponse(
            "search.json", mapOf(
                "link" to "$next,$last"
            )
        )
        val response = getValue(service.searchRepos("foo")) as ApiSuccessResponse

        assertThat(response, notNullValue())
        assertThat(response.body.total, `is`(41))
        assertThat(response.body.items.size, `is`(30))
        assertThat<String>(
            response.links["next"],
            `is`("https://api.github.com/search/repositories?q=foo&page=2")
        )
        assertThat<Int>(response.nextPage, `is`(2))
    }

    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader
            .getResourceAsStream("api-response/$fileName")
        val source = Okio.buffer(Okio.source(inputStream))
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }
}
