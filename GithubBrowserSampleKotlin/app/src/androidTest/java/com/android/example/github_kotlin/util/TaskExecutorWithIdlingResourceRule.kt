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

package com.android.example.github_kotlin.util

import android.arch.core.executor.testing.CountingTaskExecutorRule
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.IdlingResource

import org.junit.runner.Description

import java.util.concurrent.CopyOnWriteArrayList

/**
 * A Junit rule that registers Architecture Components' background threads as an Espresso idling
 * resource.
 */
class TaskExecutorWithIdlingResourceRule : CountingTaskExecutorRule() {
    private val callbacks = CopyOnWriteArrayList<IdlingResource.ResourceCallback>()
    override fun starting(description: Description?) {
        IdlingRegistry.getInstance().register(object : IdlingResource {
            override fun getName(): String {
                return "architecture components idling resource"
            }

            override fun isIdleNow(): Boolean {
                return this@TaskExecutorWithIdlingResourceRule.isIdle
            }

            override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
                callbacks.add(callback)
            }
        })
        super.starting(description)
    }

    override fun onIdle() {
        super.onIdle()
        for (callback in callbacks) {
            callback.onTransitionToIdle()
        }
    }
}
