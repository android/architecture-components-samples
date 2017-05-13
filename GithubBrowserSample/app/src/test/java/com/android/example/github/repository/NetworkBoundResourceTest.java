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

package com.android.example.github.repository;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.example.github.AppExecutors;
import com.android.example.github.api.ApiResponse;
import com.android.example.github.util.ApiUtil;
import com.android.example.github.util.CountingAppExecutors;
import com.android.example.github.util.InstantAppExecutors;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

@RunWith(Parameterized.class)
public class NetworkBoundResourceTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private Function<Foo, Void> saveCallResult;

    private Function<Foo, Boolean> shouldFetch;

    private Function<Void, LiveData<ApiResponse<Foo>>> createCall;

    private MutableLiveData<Foo> dbData = new MutableLiveData<>();

    private NetworkBoundResource<Foo, Foo> networkBoundResource;

    private AtomicBoolean fetchedOnce = new AtomicBoolean(false);
    private CountingAppExecutors countingAppExecutors;
    private final boolean useRealExecutors;

    @Parameterized.Parameters
    public static List<Boolean> param() {
        return Arrays.asList(true, false);
    }

    public NetworkBoundResourceTest(boolean useRealExecutors) {
        this.useRealExecutors = useRealExecutors;
        if (useRealExecutors) {
            countingAppExecutors = new CountingAppExecutors();
        }
    }

    @Before
    public void init() {
        AppExecutors appExecutors = useRealExecutors
                ? countingAppExecutors.getAppExecutors()
                : new InstantAppExecutors();
        networkBoundResource = new NetworkBoundResource<Foo, Foo>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull Foo item) {
                saveCallResult.apply(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable Foo data) {
                // since test methods don't handle repetitive fetching, call it only once
                return shouldFetch.apply(data) && fetchedOnce.compareAndSet(false, true);
            }

            @NonNull
            @Override
            protected LiveData<Foo> loadFromDb() {
                return dbData;
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Foo>> createCall() {
                return createCall.apply(null);
            }
        };
    }

    private void drain() {
        if (!useRealExecutors) {
            return;
        }
        try {
            countingAppExecutors.drainTasks(1, TimeUnit.SECONDS);
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }

    @Test
    public void basicFromNetwork() {
        AtomicReference<Foo> saved = new AtomicReference<>();
        shouldFetch = Objects::isNull;
        Foo fetchedDbValue = new Foo(1);
        saveCallResult = foo -> {
            saved.set(foo);
            dbData.setValue(fetchedDbValue);
            return null;
        };
        final Foo networkResult = new Foo(1);
        createCall = (aVoid) -> ApiUtil.createCall(Response.success(networkResult));

        Observer<Resource<Foo>> observer = Mockito.mock(Observer.class);
        networkBoundResource.asLiveData().observeForever(observer);
        drain();
        verify(observer).onChanged(Resource.loading(null));
        reset(observer);
        dbData.setValue(null);
        drain();
        assertThat(saved.get(), is(networkResult));
        verify(observer).onChanged(Resource.success(fetchedDbValue));
    }

    @Test
    public void failureFromNetwork() {
        AtomicBoolean saved = new AtomicBoolean(false);
        shouldFetch = Objects::isNull;
        saveCallResult = foo -> {
            saved.set(true);
            return null;
        };
        ResponseBody body = ResponseBody.create(MediaType.parse("text/html"), "error");
        createCall = (aVoid) -> ApiUtil.createCall(Response.error(500, body));

        Observer<Resource<Foo>> observer = Mockito.mock(Observer.class);
        networkBoundResource.asLiveData().observeForever(observer);
        drain();
        verify(observer).onChanged(Resource.loading(null));
        reset(observer);
        dbData.setValue(null);
        drain();
        assertThat(saved.get(), is(false));
        verify(observer).onChanged(Resource.error("error", null));
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void dbSuccessWithoutNetwork() {
        AtomicBoolean saved = new AtomicBoolean(false);
        shouldFetch = Objects::isNull;
        saveCallResult = foo -> {
            saved.set(true);
            return null;
        };

        Observer<Resource<Foo>> observer = Mockito.mock(Observer.class);
        networkBoundResource.asLiveData().observeForever(observer);
        drain();
        verify(observer).onChanged(Resource.loading(null));
        reset(observer);
        Foo dbFoo = new Foo(1);
        dbData.setValue(dbFoo);
        drain();
        verify(observer).onChanged(Resource.success(dbFoo));
        assertThat(saved.get(), is(false));
        Foo dbFoo2 = new Foo(2);
        dbData.setValue(dbFoo2);
        drain();
        verify(observer).onChanged(Resource.success(dbFoo2));
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void dbSuccessWithFetchFailure() {
        Foo dbValue = new Foo(1);
        AtomicBoolean saved = new AtomicBoolean(false);
        shouldFetch = (foo) -> foo == dbValue;
        saveCallResult = foo -> {
            saved.set(true);
            return null;
        };
        ResponseBody body = ResponseBody.create(MediaType.parse("text/html"), "error");
        MutableLiveData<ApiResponse<Foo>> apiResponseLiveData = new MutableLiveData();
        createCall = (aVoid) -> apiResponseLiveData;

        Observer<Resource<Foo>> observer = Mockito.mock(Observer.class);
        networkBoundResource.asLiveData().observeForever(observer);
        drain();
        verify(observer).onChanged(Resource.loading(null));
        reset(observer);

        dbData.setValue(dbValue);
        drain();
        verify(observer).onChanged(Resource.loading(dbValue));

        apiResponseLiveData.setValue(new ApiResponse<>(Response.error(400, body)));
        drain();
        assertThat(saved.get(), is(false));
        verify(observer).onChanged(Resource.error("error", dbValue));

        Foo dbValue2 = new Foo(2);
        dbData.setValue(dbValue2);
        drain();
        verify(observer).onChanged(Resource.error("error", dbValue2));
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void dbSuccessWithReFetchSuccess() {
        Foo dbValue = new Foo(1);
        Foo dbValue2 = new Foo(2);
        AtomicReference<Foo> saved = new AtomicReference<>();
        shouldFetch = (foo) -> foo == dbValue;
        saveCallResult = foo -> {
            saved.set(foo);
            dbData.setValue(dbValue2);
            return null;
        };
        MutableLiveData<ApiResponse<Foo>> apiResponseLiveData = new MutableLiveData();
        createCall = (aVoid) -> apiResponseLiveData;

        Observer<Resource<Foo>> observer = Mockito.mock(Observer.class);
        networkBoundResource.asLiveData().observeForever(observer);
        drain();
        verify(observer).onChanged(Resource.loading(null));
        reset(observer);

        dbData.setValue(dbValue);
        drain();
        final Foo networkResult = new Foo(1);
        verify(observer).onChanged(Resource.loading(dbValue));
        apiResponseLiveData.setValue(new ApiResponse<>(Response.success(networkResult)));
        drain();
        assertThat(saved.get(), is(networkResult));
        verify(observer).onChanged(Resource.success(dbValue2));
        verifyNoMoreInteractions(observer);
    }

    static class Foo {

        int value;

        Foo(int value) {
            this.value = value;
        }
    }
}