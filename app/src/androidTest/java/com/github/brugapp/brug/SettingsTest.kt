package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.ui.SettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Settings Activity Tests
 *
 */
@RunWith(AndroidJUnit4::class)
class SettingsTest {
    @get:Rule
    var settingsActivityRule = ActivityScenarioRule(SettingsActivity::class.java)

    @Test
    fun endToEndSettingsTest() {
//        onView(withId(R.id.mainHelloWorld)).check(ViewAssertions.matches(ViewMatchers.withText("Welcome to Unlost!")))
//        onView(withId(R.id.action_settings)).perform(click())
        onView(withId(R.id.titleSettings)).check(ViewAssertions.matches(ViewMatchers.withText("Settings")))
    }
}