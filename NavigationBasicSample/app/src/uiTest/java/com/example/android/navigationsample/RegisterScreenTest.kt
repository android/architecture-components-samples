package com.example.android.navigationsample

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @Test
    fun navTest() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            setGraph(R.navigation.navigation)
            setCurrentDestination(R.id.register)
        }

        navController.navigate(R.id.action_register_to_match)

        val backStack = navController.backStack
        assertThat(backStack).hasSize(4)
        assertThat(backStack[3].destination.id).isEqualTo(R.id.match)
        assertThat(backStack[2].destination.id).isEqualTo(R.id.register)
    }
}
