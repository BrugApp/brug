package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Main Activity Tests
 *
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    var mainActivityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun textViewDisplaysCorrectText() {
        // Context of the app under test.
        Espresso.onView(ViewMatchers.withId(R.id.mainHelloWorld))
            .check(ViewAssertions.matches(ViewMatchers.withText("Welcome to Unlost!")))
    }
}