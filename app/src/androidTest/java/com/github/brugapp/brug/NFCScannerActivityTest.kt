package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.ui.NFCScannerActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NFCScannerActivityTest {
    @get:Rule
    var qrCodeScannerActivityRule = ActivityScenarioRule(NFCScannerActivity::class.java)

    @Test
    fun correctHintText(){
        onView(withId(R.id.editTextReportItem)).check(matches((withHint("type something"))))
    }

    @Test
    fun buttonIsVisible(){
        onView(withId(R.id.nfc_found_btn)).check(matches((withText("activate tag"))))
    }

    
}