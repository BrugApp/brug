package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import androidx.test.rule.GrantPermissionRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.brugapp.brug.ui.MapsActivity
import com.github.brugapp.brug.ui.QrCodeScannerActivity
import com.github.brugapp.brug.ui.SignInActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before

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

    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(android.Manifest.permission.CAMERA)

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }


    @Test
    fun textViewDisplaysCorrectText() {
        // Context of the app under test.
        onView(withId(R.id.mainHelloWorld))
            .check(matches(withText("Welcome to Unlost!")))
    }

    @Test
    fun clickingOnCameraGoesToTheCorrectActivity() {
        onView(withId(R.id.mainCamera)).perform(click())

        Intents.intended(
            allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                hasComponent(QrCodeScannerActivity::class.java.name)
            )
        )
    }

    @Test
    fun clickingOnMapButtonGoesToTheCorrectActivity() {
        onView(withId(R.id.mapsButton)).perform(click())
        Intents.intended(
            allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                hasComponent(MapsActivity::class.java.name)
            )
        )
    }


    @Test
    fun canSeeHintWhenCameraIsClicked(){
        onView(withId(R.id.mainCamera)).perform(click())
        onView(withId(R.id.editTextReportItem))
            .check(matches((withHint("Report item…"))))
    }


    @Test
    fun clickingOnLogOnButtonGoesToCorrectActivity() {
        onView(withId(R.id.log_on_button)).perform(click())

        Intents.intended(
            allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                hasComponent(SignInActivity::class.java.name)
            )
        )
    }

}


