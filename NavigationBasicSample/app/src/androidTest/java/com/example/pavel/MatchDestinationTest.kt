import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.example.android.navigationsample.R
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.play_btn)).perform(click())
        onView(withId(R.id.signup_btn)).perform(click())
    }

    @Test
    fun testGoingToInGameDestination() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.in_game)
        }
        onView(withId(R.id.play_btn3)).perform(click())
    }
}