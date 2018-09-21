import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import com.example.android.navigationsample.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GameOverDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.play_btn)).perform(click())
        onView(withId(R.id.signup_btn)).perform(click())
        onView(withId(R.id.play_btn3)).perform(click())
        onView(withId(R.id.checkBox)).perform(click())
    }

    override fun testDestination() {
        onView(withId(R.id.game_title3)).check(matches(isDisplayed()))
    }
}