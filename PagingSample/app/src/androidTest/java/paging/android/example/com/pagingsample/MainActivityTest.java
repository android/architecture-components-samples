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

package paging.android.example.com.pagingsample;

import android.app.Activity;
import android.arch.core.executor.testing.CountingTaskExecutorRule;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Simply sanity test to ensure that activity launches without any issues and shows some data.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public CountingTaskExecutorRule testRule = new CountingTaskExecutorRule();
    @Test
    public void showSomeResults() throws InterruptedException, TimeoutException {
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        testRule.drainTasks(10, TimeUnit.SECONDS);
        RecyclerView recyclerView = activity.findViewById(R.id.cheeseList);
        MatcherAssert.assertThat(recyclerView.getAdapter(), CoreMatchers.notNullValue());
        MatcherAssert.assertThat(recyclerView.getAdapter().getItemCount() > 0,
                CoreMatchers.is(true));
    }
}
