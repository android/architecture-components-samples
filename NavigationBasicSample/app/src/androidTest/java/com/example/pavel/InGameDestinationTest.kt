import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
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

    @Test
    fun testGoingToResultsWinnerDestination() {
        val goToResultsWinnerNavigatedListener = Navigator.OnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.results_winner)
        }
        navigator.addOnNavigatorNavigatedListener(object : Navigator.OnNavigatorNavigatedListener {
            override fun onNavigatorNavigated(navigator: Navigator<*>, destId: Int, backStackEffect: Int) {
                assertEquals(destId, R.id.match)
                navigator.removeOnNavigatorNavigatedListener(this)
                navigator.addOnNavigatorNavigatedListener(goToResultsWinnerNavigatedListener)
            }
        })
        onView(withId(R.id.checkBox3)).perform(click())
    }

    @Test
    fun testGoingToGameOverDestination() {
        val goToToGameOverNavigatedListener = Navigator.OnNavigatorNavigatedListener { _, destId, _ ->
            assertEquals(destId, R.id.game_over)
        }
        navigator.addOnNavigatorNavigatedListener(object : Navigator.OnNavigatorNavigatedListener {
            override fun onNavigatorNavigated(navigator: Navigator<*>, destId: Int, backStackEffect: Int) {
                assertEquals(destId, R.id.match)
                navigator.removeOnNavigatorNavigatedListener(this)
                navigator.addOnNavigatorNavigatedListener(goToToGameOverNavigatedListener)
            }
        })
        onView(withId(R.id.checkBox)).perform(click())
    }
}