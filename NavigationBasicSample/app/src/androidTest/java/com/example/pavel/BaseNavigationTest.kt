import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.example.android.navigationsample.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseNavigationTest {

    @get:Rule
    var activityActivityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @Before
    fun init() {
        navigateToTestStartDestination()
    }

    abstract fun navigateToTestStartDestination()

    @Test
    abstract fun testDestination()
}