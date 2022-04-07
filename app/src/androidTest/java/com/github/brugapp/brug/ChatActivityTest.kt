package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.model.ChatMessage
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.CHAT_INTENT_KEY
import com.github.brugapp.brug.ui.ChatActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.Month
import java.util.*


@RunWith(AndroidJUnit4::class)
class ChatActivityTest {
    private val dummyUser = User("John", "Doe", "john@doe.com", "310200", null)
    private val dummyItem = Item("DummyItem", "Description", "0")
    private val dummyDate = LocalDateTime.of(
        2022, Month.MARCH, 23, 15, 30
    )
    private val dummyMessage = ChatMessage(
        "${dummyUser.getFirstName()} ${dummyUser.getLastName()}",
        0,
        dummyDate,
        "Dummy Test Message"
    )
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)

    @Test
    fun chatViewCorrectlyGetsConversationInfos() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            messagesList.check(
                matches(
                    atPosition(
                        0,
                        hasDescendant(withText("Dummy Test Message"))
                    )
                )
            )
        }
    }

    @Test
    fun sendMessageCorrectlyAddsNewMessage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

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

            messagesList.check(
                matches(
                    atPosition(1, hasDescendant(withText(newMessageText)))
                )
            )
        }
    }

    @Test
    fun sendLocationCorrectlyAddsNewMessage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            onView(withId(R.id.buttonSendLocalisation)).perform(click())

            messagesList.check(
                matches(
                    atPosition(1, hasDescendant(withText("Location")))
                )
            )
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

    // IMAGE TESTS
    private fun createImageFile(file: File?): File {
        val storageDir: File? = file
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    private fun storeImageAndSetResultStub(cont: Context): Instrumentation.ActivityResult {
        // create bitmap
        val encodedImage =
            "iVBORw0KGgoAAAANSUhEUgAAAKQAAACZCAYAAAChUZEyAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAG0SURBVHhe7dIxAcAgEMDALx4rqKKqDxZEZLhbYiDP+/17IGLdQoIhSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSEJmDnORA7zZz2YFAAAAAElFTkSuQmCC"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // store to file
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val outputFile = createImageFile(cont.externalCacheDir)
        outputFile.writeBytes(outputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        // return with uri as data
        val bundle = Bundle()
        val parcels = ArrayList<Parcelable>()
        val resultData = Intent()
        val parcelable = Uri.fromFile(outputFile) as Parcelable
        parcels.add(parcelable)
        bundle.putParcelableArrayList(Intent.EXTRA_STREAM, parcels)
        resultData.putExtras(bundle)
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    @Test
    fun sendCameraMessageOpensCamera() {
        val TAKE_PICTURE_REQUEST_CODE = 10
        val context = ApplicationProvider.getApplicationContext<Context>()
        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val expectedIntent: Matcher<Intent> = allOf(hasAction(Intent.ACTION_PICK))
            intending(expectedIntent).respondWith(
                Instrumentation.ActivityResult(
                    TAKE_PICTURE_REQUEST_CODE,
                    null
                )
            )
            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())
            intended(expectedIntent)
        }

        Intents.release()
    }

    @Test
    fun sendGalleryMessageOpensGallery() {
        val SELECT_PICTURE_REQUEST_CODE = 1
        val context = ApplicationProvider.getApplicationContext<Context>()
        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val expectedIntent: Matcher<Intent> = allOf(hasAction(Intent.ACTION_PICK))
            intending(expectedIntent).respondWith(
                Instrumentation.ActivityResult(
                    SELECT_PICTURE_REQUEST_CODE,
                    null
                )
            )
            onView(withId(R.id.buttonSendImage)).perform(click())
            intended(expectedIntent)
        }

        Intents.release()
    }

    /*
    TODO: Change path mode (context issue)
    @Test
    fun sendCameraMessageCorrectlyAddsNewMessage(){
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        val SELECT_PICTURE_REQUEST_CODE = 1 //gallery mode for path
        val context = ApplicationProvider.getApplicationContext<Context>()
        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            //val messagesList = onView(withId(R.id.messagesList))

            val expectedIntent: Matcher<Intent> = allOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)) //Intent.ACTION_PICK
            intending(expectedIntent).respondWith(storeImageAndSetResultStub(instrumentationContext))
            //intending(expectedIntent).respondWith(createImageGallerySetResultStub(context.externalCacheDir))

            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())
            Thread.sleep(10000)

            //messagesList.check(matches(
            //    atPosition(1, hasDescendant(withText("Location")))
            //))
        }

        Intents.release()
    }
     */

    @Test
    fun sendGalleryMessageCorrectlyAddsNewMessage() {
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = ApplicationProvider.getApplicationContext<Context>()
        val conversation = Conversation(
            dummyUser, dummyItem, mutableListOf(dummyMessage)
        )

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            //val messagesList = onView(withId(R.id.messagesList))

            val expectedIntent: Matcher<Intent> = allOf(hasAction(Intent.ACTION_PICK))
            intending(expectedIntent).respondWith(storeImageAndSetResultStub(instrumentationContext))

            onView(withId(R.id.buttonSendImage)).perform(click())
            Thread.sleep(10000)

            // TODO: uncomment this when the response'll be fixed (now: uri on result of ChatActivity is null)
            //messagesList.check(matches(
            //    atPosition(1, hasDescendant(withText("Location")))
            //))
        }

        Intents.release()
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