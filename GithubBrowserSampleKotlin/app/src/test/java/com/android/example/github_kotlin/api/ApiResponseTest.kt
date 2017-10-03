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

package com.android.example.github_kotlin.api

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Response

@RunWith(JUnit4::class)
class ApiResponseTest {
    @Test
    fun exception() {
        val exception = Exception("foo")
        val apiResponse = ApiResponse<String>(exception)
        assertThat<Map<String, String>>(apiResponse.links, notNullValue())
        assertThat<String>(apiResponse.body, nullValue())
        assertThat(apiResponse.code, `is`(500))
        assertThat<String>(apiResponse.errorMessage, `is`("foo"))
    }

    @Test
    fun success() {
        val apiResponse = ApiResponse(Response.success("foo"))
        assertThat<String>(apiResponse.errorMessage, nullValue())
        assertThat(apiResponse.code, `is`(200))
        assertThat<String>(apiResponse.body, `is`("foo"))
        assertThat<Int>(apiResponse.nextPage, `is`(nullValue()))
    }

    @Test
    fun link() {
        val link = "<https://api.github.com/search/repositories?q=foo&page=2>; rel=\"next\"," + " <https://api.github.com/search/repositories?q=foo&page=34>; rel=\"last\""
        val headers = okhttp3.Headers.of("link", link)
        val response = ApiResponse(Response.success("foo", headers))
        assertThat<Int>(response.nextPage, `is`(2))
    }

    @Test
    fun badPageNumber() {
        val link = "<https://api.github.com/search/repositories?q=foo&page=dsa>; rel=\"next\""
        val headers = okhttp3.Headers.of("link", link)
        val response = ApiResponse(Response.success("foo", headers))
        assertThat<Int>(response.nextPage, nullValue())
    }

    @Test
    fun badLinkHeader() {
        val link = "<https://api.github.com/search/repositories?q=foo&page=dsa>; relx=\"next\""
        val headers = okhttp3.Headers.of("link", link)
        val response = ApiResponse(Response.success("foo", headers))
        assertThat<Int>(response.nextPage, nullValue())
    }

    @Test
    fun error() {
        val response = ApiResponse(Response.error<String>(400,
                                                          ResponseBody.create(MediaType.parse("application/txt"), "blah")))
        assertThat(response.code, `is`(400))
        assertThat<String>(response.errorMessage, `is`("blah"))
    }
}