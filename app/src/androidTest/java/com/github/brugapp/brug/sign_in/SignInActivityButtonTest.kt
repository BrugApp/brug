package com.github.brugapp.brug.sign_in

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.github.brugapp.brug.ui.NavigationMenuActivity
import com.github.brugapp.brug.ui.QrCodeScannerActivity
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
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
        Firebase.auth.signOut()
    }

    @Test
    fun demoButtonGoesToItemsMenuActivity() {
        onView(withId(R.id.demo_button)).perform(click())

        Thread.sleep(30000)
        intended(
            allOf(
                toPackage("com.github.brugapp.brug"),
                hasComponent(ItemsMenuActivity::class.java.name)
            )
        )
    }

    @Test
    fun mapDemoButtonGoesToNavigationMenuActivity() {
        onView(withId(R.id.mapDemoButton)).perform(click())

        intended(
            allOf(
                toPackage("com.github.brugapp.brug"),
                hasComponent(NavigationMenuActivity::class.java.name)
            )
        )
    }

    @Test
    fun itemFoundButtonGoesToQRCodeScannerActivity() {
        onView(withId(R.id.qr_found_btn)).perform(click())

        intended(
            allOf(
                toPackage("com.github.brugapp.brug"),
                hasComponent(QrCodeScannerActivity::class.java.name)
            )
        )

    }

}
