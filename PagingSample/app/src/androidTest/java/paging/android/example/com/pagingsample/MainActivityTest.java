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
import androidx.arch.core.executor.testing.CountingTaskExecutorRule;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Simply sanity test to ensure that activity launches without any issues and shows some data.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public CountingTaskExecutorRule testRule = new CountingTaskExecutorRule();

    @Test
    public void showSomeResults() throws InterruptedException, TimeoutException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        testRule.drainTasks(10, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        final RecyclerView recyclerView = activity.findViewById(R.id.cheeseList);
        waitForAdapterChange(recyclerView);
        assertThat(recyclerView.getAdapter(), notNullValue());
        waitForAdapterChange(recyclerView);
        assertThat(recyclerView.getAdapter().getItemCount() > 0, is(true));
    }

    private void waitForAdapterChange(final RecyclerView recyclerView) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                recyclerView.getAdapter().registerAdapterDataObserver(
                        new RecyclerView.AdapterDataObserver() {
                            @Override
                            public void onItemRangeInserted(int positionStart, int itemCount) {
                                latch.countDown();
                            }

                            @Override
                            public void onChanged() {
                                latch.countDown();
                            }
                        });
            }
        });
        if (recyclerView.getAdapter().getItemCount() > 0) {
            return;//already loaded
        }
        assertThat(latch.await(10, TimeUnit.SECONDS), is(true));
    }
}
