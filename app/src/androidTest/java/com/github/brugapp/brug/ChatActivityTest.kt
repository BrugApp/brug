package com.github.brugapp.brug

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.model.services.DateService.Companion.fromLocalDateTime
import com.github.brugapp.brug.ui.CHAT_INTENT_KEY
import com.github.brugapp.brug.ui.ChatActivity
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.Month


@RunWith(AndroidJUnit4::class)
class ChatActivityTest {
    @get:Rule
    val permissionRule1: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    @get:Rule
    val permissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val permissionRuleAudio: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)
    @get:Rule
    val permissionRuleExtStorage: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val dummyUser = MyUser("USER1", "Rayan", "Kikou", null)

    private val dummyDate = fromLocalDateTime(
        LocalDateTime.of(
            2022, Month.MARCH, 23, 15, 30
        )
    )
    private val conversation = Conversation(
        "USER1USER2",
        dummyUser,
        "DummyItem",
        mutableListOf(Message(
            dummyUser.getFullName(), dummyDate, "TestMessage"
        ))
    )

    @Test
    fun chatViewCorrectlyGetsConversationInfos() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            messagesList.check(matches(
                atPosition(0,
                hasDescendant(withText("TestMessage"))
                )))
        }
    }

    @Test
    fun sendMessageCorrectlyAddsNewMessage(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val newMessageText = "Test sending new messages"

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            val textBox = onView(withId(R.id.editMessage))
            textBox.perform(typeText(newMessageText))
            closeSoftKeyboard()
            onView(withId(R.id.buttonSendMessage)).perform(click())

            messagesList.check(matches(
                atPosition(1, hasDescendant(withText(newMessageText)))
            ))

        }
    }

    @Test
    fun sendLocationCorrectlyAddsNewMessage(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            onView(withId(R.id.buttonSendLocalisation)).perform(click())

//            messagesList.check(matches(
//                atPosition(1, hasDescendant(withText("Location")))
//            ))
        }
    }

    @Test
    fun localisationButtonGoneAfterRecord(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.recordButton)).perform(click())
            onView(withId(R.id.buttonSendLocalisation)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun imageButtonBackAfterDeleteAudio(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.recordButton)).perform(click())
            onView(withId(R.id.deleteAudio)).perform(click())
            onView(withId(R.id.buttonSendImage)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun galleryImageButtonGoneAfterRecord(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.recordButton)).perform(click())
            onView(withId(R.id.buttonSendImage)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun recordButtonOffWhenMessage(){
        val context = ApplicationProvider.getApplicationContext<Context>()


        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        val message = "Test text"

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.editMessage)).perform(typeText(message))
            onView(withId(R.id.recordButton)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun recordButtonInAfterMessage(){
        val context = ApplicationProvider.getApplicationContext<Context>()


        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        val message = "Test text"

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.editMessage)).perform(typeText(message))
            onView(withId(R.id.buttonSendMessage)).perform(click())
            onView(withId(R.id.recordButton)).check(matches(isDisplayed()))
        }
    }

    // Helper function to match inside a RecyclerView (from StackOverflow)
    private fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder = view.findViewHolderForAdapterPosition(position)
                    ?: // has no item on such position
                    return false
                return itemMatcher.matches(viewHolder.itemView)
            }
        }
    }



    /* OLD TESTS USING FIREBASE -> COMMENTED TO BE REUSED WHEN FINAL DB IS PROPERLY SETUP */
    // DISPLAY Tests
//    @Test
//    fun checkIfReceiverFieldIsPresent() {
//        onView(withId(R.id.editName)).check(matches(withHint("Receiver")))
//    }

//    @Test
//    fun checkIfMessageFieldIsPresent() {
//        onView(withId(R.id.editMessage)).check(matches(withHint("Message")))
//    }
//
//    @Test
//    fun checkIfSendButtonIsPresent() {
//        onView(withId(R.id.buttonSendMessage)).check(matches(withText("Send")))
//    }

    // FUNCTIONALITY Tests
//    @Test
//    fun sendAndRetrieveMessageWorks() {
//        // Enter data in fields
////        onView(withId(R.id.editName)).perform(typeText("TestSender"))
////        closeSoftKeyboard()
//        onView(withId(R.id.editMessage)).perform(typeText("TestMessage"))
//        closeSoftKeyboard()
//
//        onView(withId(R.id.buttonSendMessage)).perform(click())
//
//        // Check the message
//        onData(
//            allOf(
//                `is`(instanceOf(Map::class.java)), hasEntry(
//                    equalTo("STR"),
//                    `is`("Sender: TestSender")
//                )
//            )
//        )
//    }
//    @Test
//    fun sendAndRetrieveMessageWorks() {
//        // Enter data in fields
//        onView(withId(R.id.editName)).perform(typeText("TestSender"))
//        closeSoftKeyboard()
//        onView(withId(R.id.editMessage)).perform(typeText("TestMessage"))
//        closeSoftKeyboard()
//
//        var sendButton = onView(withId(R.id.buttonSendMessage)).perform(click())
//
//        // Check the message
//        onData(
//            allOf(
//                `is`(instanceOf(Map::class.java)), hasEntry(
//                    equalTo("STR"),
//                    `is`("Sender: TestSender")
//                )
//            )
//        )
//    }
//
//    @Test
//    fun sendAndRetrieveLocalisationWorks() {
//        var sendLocalisationButton = onView(withId(R.id.buttonSendLocalisation)).perform(click())
//
//        // Check the localisation in message
//        onData(
//            allOf(
//                `is`(instanceOf(Map::class.java)), hasEntry(
//                    equalTo("STR"),
//                    `is`("Sender: Localisation service")
//                )
//            )
//        )
    }

    // TODO: See with the team if granting permissions only during tests is a good idea (better coverage)

    //@Test
    //fun localisationPermissionAsked() {
    //    onView(withId(R.id.buttonSendLocalisation)).perform(click())
    //}
//}

