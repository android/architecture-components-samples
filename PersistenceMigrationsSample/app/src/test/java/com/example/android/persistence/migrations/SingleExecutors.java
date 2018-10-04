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

package com.example.android.persistence.migrations;

import androidx.annotation.NonNull;
import java.util.concurrent.Executor;

/**
 * Allow instant execution of tasks.
 * Note: when using the Architecture components, for testing, you can use the
 * InstantTaskExecutorRule test rule, after adding
 * android.arch.core:core-testing to your build.gradle file.
 */
public class SingleExecutors extends AppExecutors {
    private static Executor instant = command -> command.run();

    public SingleExecutors() {
        super(instant, instant, instant);
    }
}
