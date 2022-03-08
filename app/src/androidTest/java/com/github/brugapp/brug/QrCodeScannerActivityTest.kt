package com.github.brugapp.brug

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class QrCodeScannerActivityTest {
    @get:Rule
    var qrCodeScannerActivityRule = ActivityScenarioRule(QrCodeScannerActivity::class.java)

    //https://stackoverflow.com/questions/33929937/android-marshmallow-test-permissions-with-espresso
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(android.Manifest.permission.CAMERA)

    @Test
    fun hintTextIsCorrect(){
        onView(withId(R.id.editTextReportItem))
            .check(matches((withHint("Report item…"))))
    }

    @Test
    fun sendInvalidId(){
        //Dumb valid ID until we have the database
        onView(withId(R.id.editTextReportItem)).perform(typeText("voiture"))
        closeSoftKeyboard()
        onView(withId(R.id.buttonReportItem)).perform(click())
        onView(withId(R.id.editTextReportItem))
            .check(matches(withText("Invalid ID")))
    }

    @Test
    fun sendValidId(){
        //Dumb valid ID until we have the database
        onView(withId(R.id.editTextReportItem)).perform(typeText("sudo1234"))
        closeSoftKeyboard()
        onView(withId(R.id.buttonReportItem)).perform(click())
        onView(withId(R.id.editTextReportItem))
            .check(matches(withText("Valid ID")))
    }

}

@RunWith(AndroidJUnit4::class)
class  QrCodeScannerActivityIntent{
    private val intent = Intent(ApplicationProvider.getApplicationContext(),QrCodeScannerActivity::class.java)

    @Test
    fun activityNoCrashOnResumeAndOnPause(){
        //https://stackoverflow.com/questions/61814729/is-there-a-way-to-pause-and-resume-activity-during-a-espresso-test
        val activityScenario = ActivityScenario.launch<QrCodeScannerActivity>(intent)
        // the activity's onCreate, onStart and onResume methods have been called at this point
        activityScenario.moveToState(Lifecycle.State.STARTED)
        // the activity's onPause method has been called at this point
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        // the activity's onResume method has been called at this point
    }
}