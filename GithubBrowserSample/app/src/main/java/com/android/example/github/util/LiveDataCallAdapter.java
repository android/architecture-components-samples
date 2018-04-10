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

package com.android.example.github.util;


import android.annotation.SuppressLint;
import android.arch.core.executor.ArchTaskExecutor;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.android.example.github.api.ApiResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.CallAdapter;

/**
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 * @param <R>
 */
public class LiveDataCallAdapter<R> implements CallAdapter<R, LiveData<ApiResponse<R>>> {
    private final Type responseType;

    LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public LiveData<ApiResponse<R>> adapt(@NonNull Call<R> call) {
        return new LiveData<ApiResponse<R>>() {

            AtomicBoolean started = new AtomicBoolean(false);

            Runnable runnable = () -> {
                try {
                    ApiResponse<R> response = new ApiResponse<>(call.execute());
                    setThreadAwareValue(response);
                } catch (IOException e) {
                    setThreadAwareValue(new ApiResponse<>(e));
                }
            };

            @Override
            protected void onActive() {
                super.onActive();
                if (started.compareAndSet(false, true)) {
                    ArchTaskExecutor.getIOThreadExecutor().execute(runnable);
                }
            }

            private void setThreadAwareValue(ApiResponse<R> value) {
                if (ArchTaskExecutor.getInstance().isMainThread()) {
                    setValue(value);
                } else {
                    postValue(value);
                }
            }
        };
    }
}
