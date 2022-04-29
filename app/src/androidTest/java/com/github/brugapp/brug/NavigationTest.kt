package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.ui.*
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule
    var mainActivityRule = ActivityScenarioRule(NavigationMenuActivity::class.java)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun clickingOnSeeItemsGoesToCorrectActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.seeItemsButton)).perform(ViewActions.click())

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(MapBoxActivity::class.java.name)
            )
        )
    }

    @Test
    fun clickingOnFreeTrackingGoesToCorrectActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.freeNavigationButton)).perform(ViewActions.click())

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(LocationTrackingActivity::class.java.name)
            )
        )
    }

    @Test
    fun clickingOnNavigateGoesToCorrectActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.navigateButton)).perform(ViewActions.click())

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(NavigationViewActivity::class.java.name)
            )
        )
    }

    @Test
    fun clickingOnMapButtonGoesToTheCorrectActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.mapsButton)).perform(ViewActions.click())
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(MapsActivity::class.java.name)
            )
        )
    }
}