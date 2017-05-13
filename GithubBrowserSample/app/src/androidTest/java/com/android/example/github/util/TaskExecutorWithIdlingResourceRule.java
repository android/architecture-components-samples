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

import org.junit.runner.Description;

import android.arch.core.executor.testing.CountingTaskExecutorRule;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Junit rule that registers Architecture Components' background threads as an Espresso idling
 * resource.
 */
public class TaskExecutorWithIdlingResourceRule extends CountingTaskExecutorRule {
    private CopyOnWriteArrayList<IdlingResource.ResourceCallback> callbacks =
            new CopyOnWriteArrayList<>();
    @Override
    protected void starting(Description description) {
        Espresso.registerIdlingResources(new IdlingResource() {
            @Override
            public String getName() {
                return "architecture components idling resource";
            }

            @Override
            public boolean isIdleNow() {
                return TaskExecutorWithIdlingResourceRule.this.isIdle();
            }

            @Override
            public void registerIdleTransitionCallback(ResourceCallback callback) {
                callbacks.add(callback);
            }
        });
        super.starting(description);
    }

    @Override
    protected void onIdle() {
        super.onIdle();
        for (IdlingResource.ResourceCallback callback : callbacks) {
            callback.onTransitionToIdle();
        }
    }
}
