/*
 * Copyright 2019, The Android Open Source Project
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

package com.example.android.navigationsample

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * A simple test class that can be run both on device (or emulator) or on the host (as a JVM test
 * using Robolectric).
 */
@RunWith(AndroidJUnit4::class)
class TitleScreenTestKotlin {

    @Test
    fun test1() {
        // Create a mock NavController
        val mockNavController = mock(NavController::class.java)

        // Create a graphical FragmentScenario for the TitleScreen
        val titleScenario = launchFragmentInContainer<TitleScreen>()

        // Set the NavController property on the fragment
        titleScenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        // Verify that performing a click prompts the correct Navigation action
        onView(withId(R.id.play_btn)).perform(click())
        verify(mockNavController).navigate(R.id.action_title_screen_to_register)
    }

    @Test
    fun navigationToRegisterTest() {
        // Create a TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            setGraph(R.navigation.navigation)
        }

        // Navigate to Register destination
        navController.navigate(R.id.action_title_screen_to_register)

        // Verify that the back stack is correct
        val backStack = navController.backStack
        assertThat(backStack).hasSize(3)
        assertThat(backStack[2].destination.id).isEqualTo(R.id.register)
        assertThat(backStack[1].destination.id).isEqualTo(R.id.title_screen)
    }

    @Test
    fun navigationToLeaderboardTest() {
        // Create a TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            setGraph(R.navigation.navigation)
        }

        // Navigate to Register fragment
        navController.navigate(R.id.action_title_screen_to_leaderboard)

        // Verify that back stack is correct
        val backStack = navController.backStack
        assertThat(backStack).hasSize(3)
        assertThat(backStack[2].destination.id).isEqualTo(R.id.leaderboard)
        assertThat(backStack[1].destination.id).isEqualTo(R.id.title_screen)
    }
}
