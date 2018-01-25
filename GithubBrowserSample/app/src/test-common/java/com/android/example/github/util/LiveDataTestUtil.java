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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LiveDataTestUtil {

    /**
     * Observes and returns the value of {@code liveData}. Note that this method is not thread-safe
     * and will probably return null when {@code liveData} executes tasks on another thread. Add
     * {@link android.arch.core.executor.testing.InstantTaskExecutorRule} to your tests in order to
     * run everything on the same thread.
     * @param liveData The {@link LiveData} to observe
     */
    public static <T> T getValue(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                data[0] = o;
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        //noinspection unchecked
        return (T) data[0];
    }
}
