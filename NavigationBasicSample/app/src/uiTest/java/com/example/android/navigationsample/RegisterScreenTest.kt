package com.example.android.navigationsample

import androidx.fragment.app.testing.launchFragmentInContainer
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

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @Test
    fun testNavigateToMatch() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        // Create a graphical FragmentScenario for the Register fragment
        val registerScenario = launchFragmentInContainer<Register>()

        // Set the NavController property on the fragment
        registerScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.navigation)
            navController.setCurrentDestination(R.id.register)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.signup_btn)).perform(click())

        val backStack = navController.backStack
        val currentDestination = backStack.last()
        assertThat(currentDestination.destination.id).isEqualTo(R.id.match)
    }
}
