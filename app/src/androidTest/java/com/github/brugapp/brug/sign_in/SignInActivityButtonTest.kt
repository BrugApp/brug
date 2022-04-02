package com.github.brugapp.brug.sign_in

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.MainActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.QrCodeScannerActivity
import com.github.brugapp.brug.ui.SignInActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SignInActivityButtonTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(SignInActivity::class.java)

    @get:Rule
    val rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun demoButtonGoesToMainActivity() {
        onView(withId(R.id.demo_button)).perform(click())

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(MainActivity::class.java.name)
            )
        )
    }

    @Test
    fun itemFoundButtonGoesToQRCodeScannerActivity() {
        onView(withId(R.id.qr_found_btn)).perform(click())

        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(QrCodeScannerActivity::class.java.name)
            )
        )

    }

}
