import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import com.example.android.navigationsample.R

class UserProfileDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.leaderboard_btn)).perform(click())
        onView(withText("Flo")).perform(click())
    }

    override fun testDestination() {
        onView(withId(R.id.profile_user_name)).check(matches(isDisplayed()))
    }
}