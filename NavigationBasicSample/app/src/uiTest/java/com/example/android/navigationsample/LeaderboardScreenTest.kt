package com.example.android.navigationsample

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LeaderboardScreenTest {

    @Test
    fun testNavigateToProfile() {
        val navController = TestNavHostController(
                ApplicationProvider.getApplicationContext()).apply {
            setGraph(R.navigation.navigation)
            setCurrentDestination(R.id.leaderboard)
        }

        // Create a graphical FragmentScenario for the Leaderboard fragment
        val leaderboardScenario = launchFragmentInContainer<Leaderboard>()

        // Set the NavController property on the fragment
        leaderboardScenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.leaderboard_list)).perform(
                actionOnItem<MyAdapter.ViewHolder>(hasDescendant(withText("Flo")), click()))

        val backStack = navController.backStack
        val currentDestination = backStack.last()
        assertThat(currentDestination.destination.id).isEqualTo(R.id.user_profile)
        assertThat(currentDestination.arguments!!["userName"]).isEqualTo("Flo")
    }
}
