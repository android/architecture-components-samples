import android.content.Intent
import android.net.Uri
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import com.example.android.navigationsample.R
import org.junit.Assert
import org.junit.Test

class UserProfileDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        //no-op
    }

    @Test
    fun testGoingToProfileDestinationViaDeepLink() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            Assert.assertEquals(destId, R.id.user_profile)
        }
        activityActivityTestRule.launchActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com/user/Flo"))
                .setPackage(getTargetContext().packageName))
        onView(ViewMatchers.withText("Flo")).check(matches(isDisplayed()))
    }
}