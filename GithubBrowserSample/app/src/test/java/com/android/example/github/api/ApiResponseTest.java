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

package com.android.example.github.api;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Header;
import retrofit2.http.Headers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class ApiResponseTest {
    @Test
    public void exception() {
        Exception exception = new Exception("foo");
        ApiResponse<String> apiResponse = new ApiResponse<>(exception);
        assertThat(apiResponse.links, notNullValue());
        assertThat(apiResponse.body, nullValue());
        assertThat(apiResponse.code, is(500));
        assertThat(apiResponse.errorMessage, is("foo"));
    }

    @Test
    public void success() {
        ApiResponse<String> apiResponse = new ApiResponse<>(Response.success("foo"));
        assertThat(apiResponse.errorMessage, nullValue());
        assertThat(apiResponse.code, is(200));
        assertThat(apiResponse.body, is("foo"));
        assertThat(apiResponse.getNextPage(), is(nullValue()));
    }

    @Test
    public void link() {
        String link = "<https://api.github.com/search/repositories?q=foo&page=2>; rel=\"next\","
                + " <https://api.github.com/search/repositories?q=foo&page=34>; rel=\"last\"";
        okhttp3.Headers headers = okhttp3.Headers.of("link", link);
        ApiResponse<String> response = new ApiResponse<>(Response.success("foo", headers));
        assertThat(response.getNextPage(), is(2));
    }

    @Test
    public void badPageNumber() {
        String link = "<https://api.github.com/search/repositories?q=foo&page=dsa>; rel=\"next\"";
        okhttp3.Headers headers = okhttp3.Headers.of("link", link);
        ApiResponse<String> response = new ApiResponse<>(Response.success("foo", headers));
        assertThat(response.getNextPage(), nullValue());
    }

    @Test
    public void badLinkHeader() {
        String link = "<https://api.github.com/search/repositories?q=foo&page=dsa>; relx=\"next\"";
        okhttp3.Headers headers = okhttp3.Headers.of("link", link);
        ApiResponse<String> response = new ApiResponse<>(Response.success("foo", headers));
        assertThat(response.getNextPage(), nullValue());
    }

    @Test
    public void error() {
        ApiResponse<String> response = new ApiResponse<String>(Response.error(400,
                ResponseBody.create(MediaType.parse("application/txt"), "blah")));
        assertThat(response.code, is(400));
        assertThat(response.errorMessage, is("blah"));
    }
}