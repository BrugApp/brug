package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Main Activity Tests
 *
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTest {
    @get:Rule
    var mainActivityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    var rule = HiltAndroidRule(this)


    @Test
    fun textViewDisplaysCorrectText() {
        // Context of the app under test.
        Espresso.onView(withId(R.id.mainHelloWorld))
            .check(ViewAssertions.matches(ViewMatchers.withText("Welcome to Unlost!")))
    }

    @Test
    fun clickingOnLogOnButtonGoesToCorrectActivity() {
        Intents.init()

        Espresso.onView(withId(R.id.log_on_button)).perform(ViewActions.click())

        Intents.intended(
            allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(SignInActivity::class.java.name)
            )
        )

        Intents.release()
    }
}