package com.android.example.github.util;

import com.android.example.github.testing.SingleFragmentActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class AutoClearedValueTest {
    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule =
            new ActivityTestRule<>(SingleFragmentActivity.class, true, true);
    public TestFragment testFragment;

    @Before
    public void init() {
        testFragment = new TestFragment();
        activityRule.getActivity().setFragment(testFragment);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void testClear() throws Throwable {
        testFragment.testValue = new AutoClearedValue<>(testFragment, "foo");
        activityRule.getActivity().replaceFragment(new TestFragment());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(testFragment.testValue.get(), nullValue());
    }

    @Test
    public void testDontClearForChildFragment() throws Throwable {
        testFragment.testValue = new AutoClearedValue<>(testFragment, "foo");
        testFragment.getChildFragmentManager().beginTransaction()
                .add(new Fragment(), "foo").commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(testFragment.testValue.get(), is("foo"));
    }

    public static class TestFragment extends Fragment {
        AutoClearedValue<String> testValue;
    }
}