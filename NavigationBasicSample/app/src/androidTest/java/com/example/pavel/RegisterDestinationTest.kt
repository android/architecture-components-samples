import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.example.android.navigationsample.R
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.play_btn)).perform(click())
    }

    @Test
    fun testGoingToMatchDestination() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.match)
        }
        onView(withId(R.id.signup_btn)).perform(click())
    }
}