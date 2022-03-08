package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
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
        Espresso.onView(ViewMatchers.withId(R.id.editTextReportItem))
            .check(ViewAssertions.matches((ViewMatchers.withHint("Report item…"))))
    }

    @Test
    fun cameraPermissionsNotGranted(){

    }
}