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
import android.databinding.DataBindingComponent
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
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.User
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
class UserFragmentTest {
    @Rule
    @JvmField
    val activityRule = ActivityTestRule(SingleFragmentActivity::class.java, true, true)
    @Rule
    @JvmField
    val executorRule = TaskExecutorWithIdlingResourceRule()
    @Rule
    @JvmField
    val countingAppExecutors = CountingAppExecutorsRule()
    private lateinit var viewModel: UserViewModel
    private lateinit var navigationController: NavigationController
    private lateinit var mockBindingAdapter: FragmentBindingAdapters
    private val userData = MutableLiveData<Resource<User>>()
    private val repoListData = MutableLiveData<Resource<List<Repo>>>()

    @Before
    fun init() {
        EspressoTestUtil.disableProgressBarAnimations(activityRule)
        val fragment = UserFragment.create("foo")
        viewModel = mock(UserViewModel::class.java)
        `when`(viewModel.user).thenReturn(userData)
        `when`(viewModel.repositories).thenReturn(repoListData)
        doNothing().`when`(viewModel).setLogin(anyString())
        navigationController = mock(NavigationController::class.java)
        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)

        fragment.appExecutors = countingAppExecutors.appExecutors
        fragment.viewModelFactory = ViewModelUtil.createFor(viewModel)
        fragment.navigationController = navigationController
        fragment.dataBindingComponent = object : DataBindingComponent {
            override fun getFragmentBindingAdapters(): FragmentBindingAdapters {
                return mockBindingAdapter
            }
        }
        activityRule.activity.setFragment(fragment)
        activityRule.runOnUiThread {
            fragment.binding.repoList.itemAnimator = null
        }
    }

    @Test
    fun loading() {
        userData.postValue(Resource.loading(null))
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())))
    }

    @Test
    fun error() {
        doNothing().`when`(viewModel).retry()
        userData.postValue(Resource.error("wtf", null))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.error_msg)).check(matches(withText("wtf")))
        onView(withId(R.id.retry)).check(matches(isDisplayed()))
        onView(withId(R.id.retry)).perform(click())
        verify(viewModel).retry()
    }

    @Test
    fun loadingWithUser() {
        val user = TestUtil.createUser("foo")
        userData.postValue(Resource.loading(user))
        onView(withId(R.id.name)).check(matches(withText(user.name)))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun loadedUser() {
        val user = TestUtil.createUser("foo")
        userData.postValue(Resource.success(user))
        onView(withId(R.id.name)).check(matches(withText(user.name)))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun loadRepos() {
        val repos = setRepos(2)
        for (pos in repos.indices) {
            val repo = repos[pos]
            onView(listMatcher().atPosition(pos)).apply {
                check(matches(hasDescendant(withText(repo.name))))
                check(matches(hasDescendant(withText(repo.description))))
                check(matches(hasDescendant(withText(repo.stars.toString()))))
            }
        }
        val repo3 = setRepos(3)[2]
        onView(listMatcher().atPosition(2)).check(
            matches(hasDescendant(withText(repo3.name)))
        )
    }

    @Test
    fun clickRepo() {
        val repos = setRepos(2)
        val selected = repos[1]
        onView(withText(selected.description)).perform(click())
        verify(navigationController).navigateToRepo(selected.owner.login, selected.name)
    }

    @Test
    fun nullUser() {
        userData.postValue(null)
        onView(withId(R.id.name)).check(matches(not(isDisplayed())))
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
        onView(withId(R.id.name)).check(matches(not(isDisplayed())))
    }

    @Test
    fun nulledRepoList() {
        setRepos(5)
        onView(listMatcher().atPosition(1)).check(matches(isDisplayed()))
        repoListData.postValue(null)
        onView(listMatcher().atPosition(0)).check(doesNotExist())
    }

    private fun listMatcher() = RecyclerViewMatcher(R.id.repo_list)

    private fun setRepos(count: Int): List<Repo> {
        val repos = (0 until count).map {
            TestUtil.createRepo("foo", "name $it", "desc$it")
        }
        repoListData.postValue(Resource.success(repos))
        return repos
    }
}