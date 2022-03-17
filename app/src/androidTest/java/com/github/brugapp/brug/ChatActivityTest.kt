package com.github.brugapp.brug

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        onView(withId(R.id.buttonSendMessage)).check(matches(withText("Send")))
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

    // NAVBAR Tests
    @Test
    fun changingBottomNavBarMenuToItemsListChangesFocus() {
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun changingBottomNavBarMenuToChatChangesFocus() {
        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
    }
}