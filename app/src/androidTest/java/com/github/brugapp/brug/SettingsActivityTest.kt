package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.ui.ProfilePictureSetActivity
import com.github.brugapp.brug.ui.SettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Settings Activity Tests
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    @get:Rule
    var settingsActivityRule = ActivityScenarioRule(SettingsActivity::class.java)

    @Test
    fun changeProfileButtonLaunchesIntent() {
        Intents.init()
        onView(withId(R.id.changeProfilePictureButton)).perform(click())
        intended(hasComponent(ProfilePictureSetActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun settingsTest() {
        Espresso.onView(ViewMatchers.withId(R.id.titleSettings))
            .check(ViewAssertions.matches(ViewMatchers.withText("Settings")))
    }
}
