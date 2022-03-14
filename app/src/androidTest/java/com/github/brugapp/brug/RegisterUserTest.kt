package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterUserTest {
    @get:Rule
    var registerUserActivityRule = ActivityScenarioRule(RegisterUser::class.java)
    //test ideas:
    //check if ProgressBar is visible after button click

    @Test
    fun registerUserActivityResultOKTest() {

        Intents.init()

        intending(
            IntentMatchers.hasComponent(
                ComponentNameMatchers.hasClassName(
                    RegisterUser::class.java.name
                )
            )
        )
            .respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent()
                )
            )

            // check if text appears correctly
            Espresso.onView(ViewMatchers.withId(R.id.newAccount))
                .check(ViewAssertions.matches(ViewMatchers.withText("Create A New Account")))
            // check if contains register button
            Espresso.onView(ViewMatchers.withId(R.id.registerbutton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Intents.release()
    }
}