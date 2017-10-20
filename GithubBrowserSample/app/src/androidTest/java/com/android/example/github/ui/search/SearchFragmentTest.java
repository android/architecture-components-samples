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

package com.android.example.github.ui.search;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;

import com.android.example.github.R;
import com.android.example.github.binding.FragmentBindingAdapters;
import com.android.example.github.testing.SingleFragmentActivity;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.util.EspressoTestUtil;
import com.android.example.github.util.RecyclerViewMatcher;
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule;
import com.android.example.github.util.TestUtil;
import com.android.example.github.util.ViewModelUtil;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SearchFragmentTest {
    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule =
            new ActivityTestRule<>(SingleFragmentActivity.class, true, true);
    @Rule
    public TaskExecutorWithIdlingResourceRule executorRule =
            new TaskExecutorWithIdlingResourceRule();

    private FragmentBindingAdapters fragmentBindingAdapters;
    private NavigationController navigationController;

    private SearchViewModel viewModel;

    private MutableLiveData<Resource<List<Repo>>> results = new MutableLiveData<>();
    private MutableLiveData<SearchViewModel.LoadMoreState> loadMoreStatus = new MutableLiveData<>();

    @Before
    public void init() {
        EspressoTestUtil.disableProgressBarAnimations(activityRule);
        SearchFragment searchFragment = new SearchFragment();
        viewModel = mock(SearchViewModel.class);
        doReturn(loadMoreStatus).when(viewModel).getLoadMoreStatus();
        when(viewModel.getResults()).thenReturn(results);

        fragmentBindingAdapters = mock(FragmentBindingAdapters.class);
        navigationController = mock(NavigationController.class);
        searchFragment.viewModelFactory = ViewModelUtil.createFor(viewModel);
        searchFragment.dataBindingComponent = () -> fragmentBindingAdapters;
        searchFragment.navigationController = navigationController;
        activityRule.getActivity().setFragment(searchFragment);
    }

    @Test
    public void search() {
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.input)).perform(typeText("foo"),
                pressKey(KeyEvent.KEYCODE_ENTER));
        verify(viewModel).setQuery("foo");
        results.postValue(Resource.loading(null));
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
    }

    @Test
    public void loadResults() {
        Repo repo = TestUtil.createRepo("foo", "bar", "desc");
        results.postValue(Resource.success(Arrays.asList(repo)));
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo/bar"))));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void dataWithLoading() {
        Repo repo = TestUtil.createRepo("foo", "bar", "desc");
        results.postValue(Resource.loading(Arrays.asList(repo)));
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo/bar"))));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void error() {
        results.postValue(Resource.error("failed to load", null));
        onView(withId(R.id.error_msg)).check(matches(isDisplayed()));
    }

    @Test
    public void loadMore() throws Throwable {
        List<Repo> repos = TestUtil.createRepos(50, "foo", "barr", "desc");
        results.postValue(Resource.success(repos));
        onView(withId(R.id.repo_list)).perform(RecyclerViewActions.scrollToPosition(49));
        onView(listMatcher().atPosition(49)).check(matches(isDisplayed()));
        verify(viewModel).loadNextPage();
    }

    @Test
    public void navigateToRepo() throws Throwable {
        doNothing().when(viewModel).loadNextPage();
        Repo repo = TestUtil.createRepo("foo", "bar", "desc");
        results.postValue(Resource.success(Arrays.asList(repo)));
        onView(withText("desc")).perform(click());
        verify(navigationController).navigateToRepo("foo", "bar");
    }

    @Test
    public void loadMoreProgress() {
        loadMoreStatus.postValue(new SearchViewModel.LoadMoreState(true, null));
        onView(withId(R.id.load_more_bar)).check(matches(isDisplayed()));
        loadMoreStatus.postValue(new SearchViewModel.LoadMoreState(false, null));
        onView(withId(R.id.load_more_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void loadMoreProgressError() {
        loadMoreStatus.postValue(new SearchViewModel.LoadMoreState(true, "QQ"));
        onView(withText("QQ")).check(matches(
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @NonNull
    private RecyclerViewMatcher listMatcher() {
        return new RecyclerViewMatcher(R.id.repo_list);
    }
}