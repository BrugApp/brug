package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.ui.SettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrugAppTest {
    @get:Rule
    var mainActivityRule = ActivityScenarioRule(SettingsActivity::class.java)

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun settingsTest() {
        Espresso.onView(ViewMatchers.withId(R.id.titleSettings))
            .check(ViewAssertions.matches(ViewMatchers.withText("Settings")))
    }
}