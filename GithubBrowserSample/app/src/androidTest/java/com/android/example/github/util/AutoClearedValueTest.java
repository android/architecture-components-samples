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

import com.android.example.github.testing.SingleFragmentActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class AutoClearedValueTest {

    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule =
            new ActivityTestRule<>(SingleFragmentActivity.class, true, true);

    private TestFragment testFragment;

    @Before
    public void init() {
        testFragment = new TestFragment();
        activityRule.getActivity().setFragment(testFragment);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void clearOnReplace() throws Throwable {
        testFragment.testValue = new AutoClearedValue<>(testFragment, "foo");
        activityRule.getActivity().replaceFragment(new TestFragment());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(testFragment.testValue.get(), nullValue());
    }

    @Test
    public void dontClearForChildFragment() throws Throwable {
        testFragment.testValue = new AutoClearedValue<>(testFragment, "foo");
        testFragment.getChildFragmentManager().beginTransaction()
                .add(new Fragment(), "foo").commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(testFragment.testValue.get(), is("foo"));
    }

    @Test
    public void dontClearForDialog() throws Throwable {
        testFragment.testValue = new AutoClearedValue<>(testFragment, "foo");
        DialogFragment dialogFragment = new DialogFragment();
        dialogFragment.show(testFragment.getFragmentManager(), "dialog");
        dialogFragment.dismiss();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(testFragment.testValue.get(), is("foo"));
    }

    public static class TestFragment extends Fragment {

        AutoClearedValue<String> testValue;
    }
}