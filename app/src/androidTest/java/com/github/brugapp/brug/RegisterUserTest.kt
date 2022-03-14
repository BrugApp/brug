package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.errorprone.annotations.DoNotMock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterUserTest {
    @get:Rule
    var registerUserActivityRule = ActivityScenarioRule(RegisterUser::class.java)
    //test ideas:
    //type in all fields and click button
    @Test
    fun registerUserTypeAndSubmitTest(){
        onView(withId(R.id.firstname)).perform(typeText("Tess"))
        closeSoftKeyboard()
        onView(withId(R.id.lastName)).perform(typeText("Terr"))
        //typing email/password and clicking button causes androidx.test.espresso.PerformException
        closeSoftKeyboard()
        onView(withId(R.id.emailAddressReg)).perform(typeText("unlost.app@gmail.com"))
        closeSoftKeyboard()
        onView(withId(R.id.PasswordReg)).perform(typeText("yoghurt"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        //Espresso.onView(ViewMatchers.withId(R.id.emailAddressReg)).check(ViewAssertions.matches(ViewMatchers.withText("unlost.app@gmail.com")))
        // check if contains register button
        Espresso.onView(ViewMatchers.withId(R.id.registerbutton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun registerUserActivityTest() {

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
            // check if contains other textboxes
        Espresso.onView(ViewMatchers.withId(R.id.firstname))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.lastName))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.emailAddressReg))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.PasswordReg))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Intents.release()
    }


    @Test
    fun newTest(){

    }
}