import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.example.android.navigationsample.R
import org.junit.Assert.assertEquals
import org.junit.Test


class TitleDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        //no-op
    }


    @Test
    fun testGoingToLeaderboardDestination() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.leaderboard)
        }
        onView(withId(R.id.leaderboard_btn)).perform(click())
    }

    @Test
    fun testGoingToRegisterDestination() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.register)
        }
        onView(withId(R.id.play_btn)).perform(click())
    }
}