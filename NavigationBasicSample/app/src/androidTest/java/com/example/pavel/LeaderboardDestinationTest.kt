import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.RecyclerView
import com.example.android.navigationsample.R
import org.junit.Assert.assertEquals
import org.junit.Test

class LeaderboardDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.leaderboard_btn)).perform(click())
    }

    @Test
    fun testGoingToUserProfileDestination() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.user_profile)
        }
        onView(withId(R.id.leaderboard_list)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withText("Flo")).check(matches(isDisplayed()))
    }
}