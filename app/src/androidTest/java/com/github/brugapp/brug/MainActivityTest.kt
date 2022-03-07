package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
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

    @Test
    fun textViewDisplaysCorrectText() {
        // Context of the app under test.
        onView(withId(R.id.mainHelloWorld))
            .check(ViewAssertions.matches(withText("Welcome to Unlost!")))
    }
}
@RunWith(AndroidJUnit4::class)
class QrCodeScannerActivityTest {
    @get:Rule
    var qrCodeScannerActivityRule = ActivityScenarioRule(QrCodeScannerActivity::class.java)

    //https://stackoverflow.com/questions/33929937/android-marshmallow-test-permissions-with-espresso
    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(android.Manifest.permission.CAMERA)

    @Test
    fun hintTextIsCorrect(){
        onView(withId(R.id.editTextReportItem))
            .check(ViewAssertions.matches((withHint("Report item…"))))
    }

}
