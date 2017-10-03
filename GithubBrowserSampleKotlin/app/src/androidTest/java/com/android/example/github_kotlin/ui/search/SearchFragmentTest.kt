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

package com.android.example.github_kotlin.ui.search

import android.arch.lifecycle.MutableLiveData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import com.android.example.github_kotlin.OpenClassOnDebug
import com.android.example.github_kotlin.R
import com.android.example.github_kotlin.binding.FragmentBindingAdapters
import com.android.example.github_kotlin.binding.FragmentDataBindingComponent
import com.android.example.github_kotlin.testing.SingleFragmentActivity
import com.android.example.github_kotlin.ui.common.NavigationController
import com.android.example.github_kotlin.util.*
import com.android.example.github_kotlin.vo.Repo
import com.android.example.github_kotlin.vo.Resource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import java.util.*

@OpenClassOnDebug
@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {
    @get:Rule
    var activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)
    @get:Rule
    var executorRule = TaskExecutorWithIdlingResourceRule()

    private lateinit var viewModel: SearchViewModel
    private lateinit var fragmentBindingAdapters: FragmentBindingAdapters
    private lateinit var navigationController: NavigationController

    private val results = MutableLiveData<Resource<List<Repo>>>()
    private val loadMoreStatus = MutableLiveData<SearchViewModel.LoadMoreState>()

    @Before
    fun init() {
        fragmentBindingAdapters = mock()
        val fragmentDataBindingComponent: FragmentDataBindingComponent = mock()
        whenever(fragmentDataBindingComponent.fragmentBindingAdapters).thenReturn(fragmentBindingAdapters)

        viewModel = mock()
        whenever(viewModel.loadMoreStatus).thenReturn(loadMoreStatus)
        whenever(viewModel.results).thenReturn(results)

        navigationController = mock()

        val searchFragment = SearchFragment()
        searchFragment.viewModelFactory = ViewModelUtil.createFor<SearchViewModel>(viewModel)
        searchFragment.dataBindingComponent = fragmentDataBindingComponent
        searchFragment.navigationController = navigationController
        activityRule.activity.setFragment(searchFragment)
    }

    @Test
    fun search() {
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.input)).perform(typeText("foo"),
                                           pressKey(KeyEvent.KEYCODE_ENTER))
        verify<SearchViewModel>(viewModel).setQuery("foo")
        results.postValue(Resource.loading<List<Repo>>(null))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
    }

    @Test
    fun loadResults() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        results.postValue(Resource.success(Arrays.asList(repo)))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo/bar"))))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun dataWithLoading() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        results.postValue(Resource.loading(Arrays.asList(repo)))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo/bar"))))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun error() {
        results.postValue(Resource.error<List<Repo>>("failed to load", null))
        onView(withId(R.id.error_msg)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Throwable::class)
    fun loadMore() {
        val repos = TestUtil.createRepos(50, "foo", "barr", "desc")
        results.postValue(Resource.success(repos))
        onView(withId(R.id.repo_list)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(49))
        onView(listMatcher().atPosition(49)).check(matches(isDisplayed()))
        verify<SearchViewModel>(viewModel).loadNextPage()
    }

    @Test
    @Throws(Throwable::class)
    fun navigateToRepo() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        results.postValue(Resource.success(Arrays.asList(repo)))
        onView(withText("desc")).perform(click())
        verify<NavigationController>(navigationController).navigateToRepo("foo", "bar")
    }

    @Test
    fun loadMoreProgress() {
        loadMoreStatus.postValue(SearchViewModel.LoadMoreState(true, null))
        onView(withId(R.id.load_more_bar)).check(matches(isDisplayed()))
        loadMoreStatus.postValue(SearchViewModel.LoadMoreState(false, null))
        onView(withId(R.id.load_more_bar)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun loadMoreProgressError() {
        loadMoreStatus.postValue(SearchViewModel.LoadMoreState(true, "QQ"))
        onView(withText("QQ")).check(matches(
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.repo_list)
    }
}