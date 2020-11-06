package com.example.playground

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.playground.ui.home.FragmentHome
import com.example.playground.ui.nav.SecondFragment
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Not working
 */
@RunWith(AndroidJUnit4::class)
class FragmentTests {
    @Test fun testSpecificFragment() {
        val scenario = launchFragmentInContainer<FragmentHome>()
        onView(
            withId(R.id.btn_nav_another_fragment)
        ).check(
            matches(
                withText("Navigate to test Global Nav actions")
            )
        )
        scenario.recreate()
    }
    @Test
    fun navTest() {
        // Create a TestNavHostController
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        navController.setGraph(R.navigation.navigation_graph)

        // Create a graphical Fragment
        val scenarioSecond = launchFragmentInContainer<SecondFragment>()

        // Check
        onView(withId(R.id.btn_go_to_three)).check(matches(isDisplayed()))

        // Set the NAvController property on the fragment
        scenarioSecond.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Verify that performing a click changes the NavController's state
        onView(ViewMatchers.withId(R.id.btn_go_to_three)).perform(ViewActions.click())
    }
}