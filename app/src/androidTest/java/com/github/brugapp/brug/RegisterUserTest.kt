package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterUserTest {
    @get:Rule
    var registerUserActivityRule = ActivityScenarioRule(RegisterUser::class.java)

    @Test
    fun registerUserTypeAndSubmitTest(){
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.firstname)).perform(typeText("Tess"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.lastName)).perform(typeText("Terr"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.PasswordReg)).perform(typeText("short"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.PasswordReg)).perform(typeText("yoghurtman"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.emailAddressReg)).perform(typeText("bademail.com"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.emailAddressReg)).perform(clearText())
        onView(withId(R.id.emailAddressReg)).perform(typeText("unlost.app@gmail.com"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        onView(withId(R.id.emailAddressReg)).perform(clearText())
        onView(withId(R.id.emailAddressReg)).perform(typeText("freshemail@gmail.com"))
        closeSoftKeyboard()
        onView(withId(R.id.registerbutton)).perform(click())
        // check if contains register button
        onView(ViewMatchers.withId(R.id.registerbutton))
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
        onView(ViewMatchers.withId(R.id.newAccount))
            .check(ViewAssertions.matches(ViewMatchers.withText("Create A New Account")))
        // check if contains register button
        onView(ViewMatchers.withId(R.id.registerbutton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        // check if contains other textboxes
        onView(ViewMatchers.withId(R.id.firstname))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.lastName))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.emailAddressReg))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.PasswordReg))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Intents.release()
    }
}