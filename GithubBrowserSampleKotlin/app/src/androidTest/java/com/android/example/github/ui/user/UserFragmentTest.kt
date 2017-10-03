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

package com.android.example.github.ui.user

import android.arch.lifecycle.MutableLiveData
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
import com.android.example.github.util.RecyclerViewMatcher
import com.android.example.github.util.TestUtil
import com.android.example.github.util.ViewModelUtil
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.User
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
class UserFragmentTest {
    @get:Rule
    var activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)

    private lateinit var viewModel: UserViewModel
    private lateinit var fragmentBindingAdapters: FragmentBindingAdapters
    private lateinit var navigationController: NavigationController

    private val userData = MutableLiveData<Resource<User>>()
    private val repoListData = MutableLiveData<Resource<List<Repo>>>()

    @Before
    fun init() {
        fragmentBindingAdapters = mock()
        val fragmentDataBindingComponent: FragmentDataBindingComponent = mock()
        whenever(fragmentDataBindingComponent.fragmentBindingAdapters).thenReturn(fragmentBindingAdapters)

        viewModel = mock()
        whenever(viewModel.user).thenReturn(userData)
        whenever(viewModel.repositories).thenReturn(repoListData)

        navigationController = mock()

        val fragment = UserFragment.create("foo")
        fragment.viewModelFactory = ViewModelUtil.createFor<UserViewModel>(viewModel)
        fragment.navigationController = navigationController
        fragment.dataBindingComponent = fragmentDataBindingComponent

        activityRule.activity.setFragment(fragment)
    }

    @Test
    fun loading() {
        userData.postValue(Resource.loading<User>(null))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun error() {
        userData.postValue(Resource.error<User>("wtf", null))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.error_msg)).check(matches(withText("wtf")))
        onView(withId(R.id.retry)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).perform(click())
        verify<UserViewModel>(viewModel).retry()
    }

    @Test
    fun loadingWithUser() {
        val user = TestUtil.createUser("foo")
        userData.postValue(Resource.loading(user))
        onView(withId(R.id.name))
                .check(matches(withText(user.name)))
        onView(withId(R.id.progress_bar))
                .check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun loadedUser() {
        val user = TestUtil.createUser("foo")
        userData.postValue(Resource.success(user))
        onView(withId(R.id.name)).check(matches(withText(user.name)))
        onView(withId(R.id.progress_bar)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun loadRepos() {
        val repos = setRepos(2)
        for (pos in repos.indices) {
            val (_, name, _, description, _, stars) = repos[pos]
            onView(listMatcher().atPosition(pos))
                    .check(matches(hasDescendant(withText(name))))
            onView(listMatcher().atPosition(pos))
                    .check(matches(hasDescendant(withText(description))))
            onView(listMatcher().atPosition(pos))
                    .check(matches(hasDescendant(withText("$stars"))))
        }
        val (_, name) = setRepos(3)[2]

        // Todo: This is a massive hack that needs a proper resolution
        Thread.sleep(10)

        onView(listMatcher().atPosition(2))
                .check(matches(hasDescendant(withText(name))))
    }

    @Test
    fun clickRepo() {
        val repos = setRepos(2)
        val (_, name, _, description, owner) = repos[1]
        onView(withText(description)).perform(click())
        verify<NavigationController>(navigationController).navigateToRepo(owner.login, name)
    }

    @Test
    fun nullUser() {
        userData.postValue(null)
        onView(withId(R.id.name)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun nullRepoList() {
        repoListData.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    @Test
    fun nulledUser() {
        val user = TestUtil.createUser("foo")
        userData.postValue(Resource.success(user))
        onView(withId(R.id.name)).check(matches(withText(user.name)))
        userData.postValue(null)
        onView(withId(R.id.name)).check(matches(not<View>(isDisplayed())))
    }

    @Test
    fun nulledRepoList() {
        setRepos(5)
        onView(listMatcher().atPosition(1)).check(matches(isDisplayed()))
        repoListData.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.repo_list)
    }

    private fun setRepos(count: Int): List<Repo> {
        val repos = (0 until count).map { TestUtil.createRepo("foo", "name $it", "desc $it") }
        repoListData.postValue(Resource.success(repos))
        return repos
    }
}