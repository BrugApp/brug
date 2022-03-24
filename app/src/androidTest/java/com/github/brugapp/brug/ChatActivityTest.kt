package com.github.brugapp.brug

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.ui.ChatActivity
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ChatActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(ChatActivity::class.java)

    // DISPLAY Tests
    @Test
    fun checkIfReceiverFieldIsPresent() {
        onView(withId(R.id.editName)).check(matches(withHint("Receiver")))
    }

    @Test
    fun checkIfMessageFieldIsPresent() {
        onView(withId(R.id.editMessage)).check(matches(withHint("Message")))
    }

    @Test
    fun checkIfSendButtonIsPresent() {
        onView(withId(R.id.buttonSendMessage)).check(matches(withContentDescription("Send")))
    }

    @Test
    fun checkIfSendLocalisationButtonIsPresent() {
        onView(withId(R.id.buttonSendLocalisation)).check(matches(withContentDescription("Send localisation")))
    }

    // FUNCTIONALITY Tests
    @Test
    fun sendAndRetrieveMessageWorks() {
        // Enter data in fields
        onView(withId(R.id.editName)).perform(typeText("TestSender"))
        closeSoftKeyboard()
        onView(withId(R.id.editMessage)).perform(typeText("TestMessage"))
        closeSoftKeyboard()

        var sendButton = onView(withId(R.id.buttonSendMessage)).perform(click())

        // Check the message
        onData(
            allOf(
                `is`(instanceOf(Map::class.java)), hasEntry(
                    equalTo("STR"),
                    `is`("Sender: TestSender")
                )
            )
        )
    }

    @Test
    fun sendAndRetrieveLocalisationWorks() {
        var sendLocalisationButton = onView(withId(R.id.buttonSendLocalisation)).perform(click())

        // Check the localisation in message
        onData(
            allOf(
                `is`(instanceOf(Map::class.java)), hasEntry(
                    equalTo("STR"),
                    `is`("Sender: Localisation service")
                )
            )
        )
    }

    // TODO: See with the team if granting permissions only during tests is a good idea (better coverage)
    //@get:Rule var permissionRule1: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    //@get:Rule var permissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    //@Test
    //fun localisationPermissionAsked() {
    //    onView(withId(R.id.buttonSendLocalisation)).perform(click())
    //}
}