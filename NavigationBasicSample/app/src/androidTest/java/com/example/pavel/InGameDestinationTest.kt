import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import androidx.navigation.Navigator
import com.example.android.navigationsample.R
import junit.framework.Assert.assertEquals
import org.junit.Test

class InGameDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.play_btn)).perform(click())
        onView(withId(R.id.signup_btn)).perform(click())
        onView(withId(R.id.play_btn3)).perform(click())
    }

    override fun testDestination() {
        onView(withId(R.id.textView2)).check(matches(isDisplayed()))
    }
}