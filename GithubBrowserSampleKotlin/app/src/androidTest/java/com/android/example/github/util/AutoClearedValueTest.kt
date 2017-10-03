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
package com.android.example.github.util

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import com.android.example.github.testing.SingleFragmentActivity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoClearedValueTest {

    @Suppress("MemberVisibilityCanPrivate")
    @get:Rule
    var activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)

    private lateinit var testFragment: TestFragment

    @Before
    fun init() {
        testFragment = TestFragment()
        activityRule.activity.setFragment(testFragment)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    @Throws(Throwable::class)
    fun clearOnReplace() {
        testFragment.testValue = AutoClearedValue(testFragment, "foo")
        activityRule.activity.replaceFragment(TestFragment())
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat<String>(testFragment.testValue!!.get(), nullValue())
    }

    @Test
    @Throws(Throwable::class)
    fun dontClearForChildFragment() {
        testFragment.testValue = AutoClearedValue(testFragment, "foo")
        testFragment.childFragmentManager.beginTransaction()
                .add(Fragment(), "foo").commit()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat<String>(testFragment.testValue!!.get(), `is`("foo"))
    }

    @Test
    @Throws(Throwable::class)
    fun dontClearForDialog() {
        testFragment.testValue = AutoClearedValue(testFragment, "foo")
        val dialogFragment = DialogFragment()
        dialogFragment.show(testFragment.fragmentManager, "dialog")
        dialogFragment.dismiss()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat<String>(testFragment.testValue!!.get(), `is`("foo"))
    }

    class TestFragment : Fragment() {
        internal var testValue: AutoClearedValue<String>? = null
    }
}