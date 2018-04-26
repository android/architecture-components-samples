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

package com.android.example.github.ui.repo

import android.arch.lifecycle.MutableLiveData
import android.databinding.DataBindingComponent
import android.support.annotation.StringRes
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.android.example.github.R
import com.android.example.github.binding.FragmentBindingAdapters
import com.android.example.github.testing.SingleFragmentActivity
import com.android.example.github.ui.common.NavigationController
import com.android.example.github.util.CountingAppExecutorsRule
import com.android.example.github.util.EspressoTestUtil
import com.android.example.github.util.RecyclerViewMatcher
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule
import com.android.example.github.util.TestUtil
import com.android.example.github.util.ViewModelUtil
import com.android.example.github.vo.Contributor
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class RepoFragmentTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)
    @Rule
    @JvmField
    val executorRule = TaskExecutorWithIdlingResourceRule()
    @Rule
    @JvmField
    val countingAppExecutors = CountingAppExecutorsRule()
    private val repoLiveData = MutableLiveData<Resource<Repo>>()
    private val contributorsLiveData = MutableLiveData<Resource<List<Contributor>>>()
    private lateinit var repoFragment: RepoFragment
    private lateinit var viewModel: RepoViewModel
    private lateinit var mockBindingAdapter: FragmentBindingAdapters
    private lateinit var navigationController: NavigationController


    @Before
    fun init() {
        EspressoTestUtil.disableProgressBarAnimations(activityRule)
        repoFragment = RepoFragment.create("a", "b")
        viewModel = mock(RepoViewModel::class.java)
        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)
        navigationController = mock(NavigationController::class.java)
        doNothing().`when`(viewModel).setId(anyString(), anyString())
        `when`(viewModel.repo).thenReturn(repoLiveData)
        `when`(viewModel.contributors).thenReturn(contributorsLiveData)
        repoFragment.appExecutors = countingAppExecutors.appExecutors
        repoFragment.viewModelFactory = ViewModelUtil.createFor(viewModel)
        repoFragment.dataBindingComponent = object : DataBindingComponent {
            override fun getFragmentBindingAdapters(): FragmentBindingAdapters {
                return mockBindingAdapter
            }
        }
        repoFragment.navigationController = navigationController
        activityRule.activity.setFragment(repoFragment)
    }

    @Test
    fun testLoading() {
        repoLiveData.postValue(Resource.loading(null))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testValueWhileLoading() {
        val repo = TestUtil.createRepo("yigit", "foo", "foo-bar")
        repoLiveData.postValue(Resource.loading(repo))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.name)).check(
            matches(
                withText(getString(R.string.repo_full_name, "yigit", "foo"))
            )
        )
        onView(withId(R.id.description)).check(matches(withText("foo-bar")))
    }

    @Test
    fun testLoaded() {
        val repo = TestUtil.createRepo("foo", "bar", "buzz")
        repoLiveData.postValue(Resource.success(repo))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.name)).check(
            matches(
                withText(getString(R.string.repo_full_name, "foo", "bar"))
            )
        )
        onView(withId(R.id.description)).check(matches(withText("buzz")))
    }

    @Test
    fun testError() {
        repoLiveData.postValue(Resource.error("foo", null))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.retry)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).perform(click())
        verify(viewModel).retry()
        repoLiveData.postValue(Resource.loading(null))

        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())))
        val repo = TestUtil.createRepo("owner", "name", "desc")
        repoLiveData.postValue(Resource.success(repo))

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())))
        onView(withId(R.id.name)).check(
            matches(
                withText(getString(R.string.repo_full_name, "owner", "name"))
            )
        )
        onView(withId(R.id.description)).check(matches(withText("desc")))
    }

    @Test
    fun testContributors() {
        setContributors("aa", "bb")
        onView(listMatcher().atPosition(0))
            .check(matches(hasDescendant(withText("aa"))))
        onView(listMatcher().atPosition(1))
            .check(matches(hasDescendant(withText("bb"))))
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.contributor_list)
    }

    @Test
    fun testContributorClick() {
        setContributors("aa", "bb", "cc")
        onView(withText("cc")).perform(click())
        verify(navigationController).navigateToUser("cc")
    }

    @Test
    fun nullRepo() {
        repoLiveData.postValue(null)
        onView(withId(R.id.name)).check(matches(not(isDisplayed())))
    }

    @Test
    fun nullContributors() {
        setContributors("a", "b", "c")
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("a"))))
        contributorsLiveData.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    private fun setContributors(vararg names: String) {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        val contributors = names.mapIndexed { index, name ->
            TestUtil.createContributor(
                repo = repo,
                login = name,
                contributions = 100 - index
            )
        }
        contributorsLiveData.postValue(Resource.success(contributors))
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String {
        return InstrumentationRegistry.getTargetContext().getString(id, *args)
    }
}