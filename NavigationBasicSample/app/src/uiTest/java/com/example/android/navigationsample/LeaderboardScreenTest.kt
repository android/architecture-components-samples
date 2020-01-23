package com.example.android.navigationsample

import androidx.core.os.bundleOf
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LeaderboardScreenTest {

    @Test
    fun navTest() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            setGraph(R.navigation.navigation)
            setCurrentDestination(R.id.leaderboard)
        }

        val bundle = bundleOf("userName" to "test")
        navController.navigate(R.id.action_leaderboard_to_userProfile, bundle)

        val backStack = navController.backStack
        assertThat(backStack).hasSize(4)
        assertThat(backStack[3].destination.id).isEqualTo(R.id.user_profile)
        assertThat(backStack[3].destination.arguments).containsKey("userName")
        assertThat(backStack[2].destination.id).isEqualTo(R.id.leaderboard)
    }
}
