/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.android.persistence.ui;


import android.arch.core.executor.testing.CountingTaskExecutorRule;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

import com.example.android.persistence.EspressoTestUtil;
import com.example.android.persistence.R;
import com.example.android.persistence.ui.MainActivity;

public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Rule
    public CountingTaskExecutorRule mCountingTaskExecutorRule = new CountingTaskExecutorRule();

    @Before
    public void disableRecyclerViewAnimations() {
        EspressoTestUtil.disableAnimations(mActivityRule);
    }

    @Test
    public void clickOnFirstItem_opensComments() throws TimeoutException, InterruptedException {
        drain();
        // When clicking on the first product
        onView(ViewMatchers.withContentDescription(R.string.cd_products_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        drain();
        // Then the second screen with the comments should appear.
        onView(withContentDescription(R.string.cd_comments_list))
                .check(matches(isDisplayed()));
        drain();
        // Then the second screen with the comments should appear.
        onView(withContentDescription(R.string.cd_product_name))
                .check(matches(not(withText(""))));

    }

    private void drain() throws TimeoutException, InterruptedException {
        mCountingTaskExecutorRule.drainTasks(1, TimeUnit.MINUTES);
    }
}