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

import com.android.example.github.util.LiveDataCallAdapterFactory;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import android.arch.core.executor.testing.InstantTaskExecutorRule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.android.example.github.util.LiveDataTestUtil.getValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class GithubServiceTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private GithubService service;

    private MockWebServer mockWebServer;

    @Before
    public void createService() throws IOException {
        mockWebServer = new MockWebServer();
        service = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(GithubService.class);
    }

    @After
    public void stopService() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void getUser() throws IOException, InterruptedException {
        enqueueResponse("user-yigit.json");
        User yigit = getValue(service.getUser("yigit")).body;

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath(), is("/users/yigit"));

        assertThat(yigit, notNullValue());
        assertThat(yigit.avatarUrl, is("https://avatars3.githubusercontent.com/u/89202?v=3"));
        assertThat(yigit.company, is("Google"));
        assertThat(yigit.blog, is("birbit.com"));
    }

    @Test
    public void getRepos() throws IOException, InterruptedException {
        enqueueResponse("repos-yigit.json");
        List<Repo> repos = getValue(service.getRepos("yigit")).body;

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath(), is("/users/yigit/repos"));

        assertThat(repos.size(), is(2));

        Repo repo = repos.get(0);
        assertThat(repo.fullName, is("yigit/AckMate"));

        Repo.Owner owner = repo.owner;
        assertThat(owner, notNullValue());
        assertThat(owner.login, is("yigit"));
        assertThat(owner.url, is("https://api.github.com/users/yigit"));

        Repo repo2 = repos.get(1);
        assertThat(repo2.fullName, is("yigit/android-architecture"));
    }

    @Test
    public void getContributors() throws IOException, InterruptedException {
        enqueueResponse("contributors.json");
        List<Contributor> contributors = getValue(
                service.getContributors("foo", "bar")).body;
        assertThat(contributors.size(), is(3));
        Contributor yigit = contributors.get(0);
        assertThat(yigit.getLogin(), is("yigit"));
        assertThat(yigit.getAvatarUrl(), is("https://avatars3.githubusercontent.com/u/89202?v=3"));
        assertThat(yigit.getContributions(), is(291));
        assertThat(contributors.get(1).getLogin(), is("guavabot"));
        assertThat(contributors.get(2).getLogin(), is("coltin"));
    }

    @Test
    public void search() throws IOException, InterruptedException {
        String header = "<https://api.github.com/search/repositories?q=foo&page=2>; rel=\"next\","
                + " <https://api.github.com/search/repositories?q=foo&page=34>; rel=\"last\"";
        Map<String, String> headers = new HashMap<>();
        headers.put("link", header);
        enqueueResponse("search.json", headers);
        ApiResponse<RepoSearchResponse> response = getValue(
                service.searchRepos("foo"));

        assertThat(response, notNullValue());
        assertThat(response.body.getTotal(), is(41));
        assertThat(response.body.getItems().size(), is(30));
        assertThat(response.links.get("next"),
                is("https://api.github.com/search/repositories?q=foo&page=2"));
        assertThat(response.getNextPage(), is(2));
    }

    private void enqueueResponse(String fileName) throws IOException {
        enqueueResponse(fileName, Collections.emptyMap());
    }

    private void enqueueResponse(String fileName, Map<String, String> headers) throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("api-response/" + fileName);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        MockResponse mockResponse = new MockResponse();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            mockResponse.addHeader(header.getKey(), header.getValue());
        }
        mockWebServer.enqueue(mockResponse
                .setBody(source.readString(StandardCharsets.UTF_8)));
    }
}
