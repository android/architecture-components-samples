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
import android.arch.paging.PagedList
import android.support.annotation.StringRes
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import com.android.example.github.OpenClassOnDebug
import com.android.example.github.R
import com.android.example.github.binding.FragmentBindingAdapters
import com.android.example.github.binding.FragmentDataBindingComponent
import com.android.example.github.testing.SingleFragmentActivity
import com.android.example.github.ui.common.NavigationController
import com.android.example.github.util.*
import com.android.example.github.vo.Contributor
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

@OpenClassOnDebug
@RunWith(AndroidJUnit4::class)
class RepoFragmentTest {
    @get:Rule
    var activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)

    private val repo = MutableLiveData<Resource<Repo>>()
    private val contributors = MutableLiveData<Resource<PagedList<Contributor>>>()
    private lateinit var repoFragment: RepoFragment
    private lateinit var viewModel: RepoViewModel

    private lateinit var fragmentBindingAdapters: FragmentBindingAdapters
    private lateinit var navigationController: NavigationController

    @Before
    fun init() {
        fragmentBindingAdapters = mock()
        val fragmentDataBindingComponent: FragmentDataBindingComponent = mock()
        whenever(fragmentDataBindingComponent.fragmentBindingAdapters).thenReturn(fragmentBindingAdapters)

        viewModel = mock()
        whenever(viewModel.repo).thenReturn(repo)
        whenever(viewModel.contributors).thenReturn(contributors)

        navigationController = mock()

        repoFragment = RepoFragment.create("a", "b")
        repoFragment.viewModelFactory = ViewModelUtil.createFor<RepoViewModel>(viewModel)
        repoFragment.dataBindingComponent = fragmentDataBindingComponent
        repoFragment.navigationController = navigationController


        activityRule.activity.setFragment(repoFragment)
    }

    @Test
    fun testLoading() {
        repo.postValue(Resource.loading<Repo>(null))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun testValueWhileLoading() {
        val repo = TestUtil.createRepo("yigit", "foo", "foo-bar")
        this.repo.postValue(Resource.loading(repo))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.name)).check(matches(
                withText(getString(R.string.repo_full_name, "yigit", "foo"))))
        onView(withId(R.id.description)).check(matches(withText("foo-bar")))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLoaded() {
        val repo = TestUtil.createRepo("foo", "bar", "buzz")
        this.repo.postValue(Resource.success(repo))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.name)).check(matches(
                withText(getString(R.string.repo_full_name, "foo", "bar"))))
        onView(withId(R.id.description)).check(matches(withText("buzz")))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testError() {
        repo.postValue(Resource.error<Repo>("foo", null))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.retry)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).perform(click())
        verify<RepoViewModel>(viewModel).retry()
        repo.postValue(Resource.loading<Repo>(null))

        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).check(matches(not<View>(isDisplayed())))
        val repo = TestUtil.createRepo("owner", "name", "desc")
        this.repo.postValue(Resource.success(repo))

        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.retry)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.name)).check(matches(
                withText(getString(R.string.repo_full_name, "owner", "name"))))
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
        verify<NavigationController>(navigationController).navigateToUser("cc")
    }

    @Test
    fun nullRepo() {
        this.repo.postValue(null)
        onView(withId(R.id.name)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun nullContributors() {
        setContributors("a", "b", "c")
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("a"))))
        contributors.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    private fun setContributors(vararg names: String) {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        var contributionCount = 100
        val contributors = names.map { TestUtil.createContributor(repo, it, contributionCount--) }

        this.contributors.postValue(Resource.success(PagedListUtil.from(contributors)))
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String {
        return InstrumentationRegistry.getTargetContext().getString(id, *args)
    }
}