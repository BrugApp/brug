package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
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

    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(android.Manifest.permission.CAMERA)

    @Test
    fun textViewDisplaysCorrectText() {
        // Context of the app under test.
        onView(withId(R.id.mainHelloWorld))
            .check(matches(withText("Welcome to Unlost!")))
    }


    @Test
    fun canSeeHintWhenCameraIsClicked(){
        onView(withId(R.id.mainCamera)).perform(click())
        onView(withId(R.id.editTextReportItem))
            .check(matches((withHint("Report item…"))))
    }
}
