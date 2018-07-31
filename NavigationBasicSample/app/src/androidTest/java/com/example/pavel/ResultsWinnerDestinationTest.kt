import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import androidx.navigation.Navigator
import com.example.android.navigationsample.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultsWinnerDestinationTest : BaseNavigationTest() {

    override fun navigateToTestStartDestination() {
        onView(withId(R.id.play_btn)).perform(click())
        onView(withId(R.id.signup_btn)).perform(click())
        onView(withId(R.id.play_btn3)).perform(click())
        onView(withId(R.id.checkBox3)).perform(click())
    }

    @Test
    fun testGoingToLeaderboardDestination() {
        val goToLeaderboardDestinationListener = Navigator.OnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.leaderboard)
        }
        val goToTitleDestinationListener = object : Navigator.OnNavigatorNavigatedListener {
            override fun onNavigatorNavigated(navigator: Navigator<*>, destId: Int, backStackEffect: Int) {
                assertEquals(destId, R.id.title_screen)
                navigator.removeOnNavigatorNavigatedListener(this)
                navigator.addOnNavigatorNavigatedListener(goToLeaderboardDestinationListener)
            }
        }
        val goToRegisterDestinationListener = object : Navigator.OnNavigatorNavigatedListener {
            override fun onNavigatorNavigated(navigator: Navigator<*>, destId: Int, backStackEffect: Int) {
                assertEquals(destId, R.id.register)
                navigator.removeOnNavigatorNavigatedListener(this)
                navigator.addOnNavigatorNavigatedListener(goToTitleDestinationListener)
            }
        }
        navigator.addOnNavigatorNavigatedListener(object : Navigator.OnNavigatorNavigatedListener {
            override fun onNavigatorNavigated(navigator: Navigator<*>, destId: Int, backStackEffect: Int) {
                assertEquals(destId, R.id.match)
                navigator.removeOnNavigatorNavigatedListener(this)
                navigator.addOnNavigatorNavigatedListener(goToRegisterDestinationListener)
            }
        })
        onView(withId(R.id.leaderboard_btn2)).perform(click())
    }

    @Test
    fun testGoingToMatchDestination() {
        navigator.addOnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.match)
        }
        onView(withId(R.id.play_btn2)).perform(click())
    }
}