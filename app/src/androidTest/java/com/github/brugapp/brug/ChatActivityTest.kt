package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
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
import org.hamcrest.Matchers.allOf
import org.junit.Rule
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
    @get:Rule
    val permissionRule1: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val permissionRule2: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val permissionRuleAudio: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @get:Rule
    val permissionRuleExtStorage: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val permissionRuleCamera: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

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
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)
    private lateinit var testUri: Uri

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
    fun sendMessageCorrectlyAddsNewMessage() {
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

            messagesList.check(
                matches(
                    atPosition(1, hasDescendant(withText(newMessageText)))
                )
            )
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
    fun localisationButtonGoneAfterRecord() {
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
    fun imageButtonBackAfterDeleteAudio() {
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
    fun galleryImageButtonGoneAfterRecord() {
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
    fun recordButtonOffWhenMessage() {
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
    fun recordButtonInAfterMessage() {
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

    private fun createImageFile(cont: Context): File {
        val storageDir: File? = cont.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    private fun storeImageAndSetResultStub(cont: Context): Instrumentation.ActivityResult {
        // create bitmap
        // below is a base64 blue image
        val encodedImage =
            "iVBORw0KGgoAAAANSUhEUgAAAKQAAACZCAYAAAChUZEyAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAG0SURBVHhe7dIxAcAgEMDALx4rqKKqDxZEZLhbYiDP+/17IGLdQoIhSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSEJmDnORA7zZz2YFAAAAAElFTkSuQmCC"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // store to outputstream
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        // create uri for file
        val imageFile = createImageFile(cont)
        val uri = FileProvider.getUriForFile(
            cont,
            "com.github.brugapp.brug.fileprovider",
            imageFile
        )
        testUri = uri

        // store bitmap to file
        imageFile.writeBytes(outputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        // return with uri as data
        val resultData = Intent()
        resultData.putExtra("imageUri", uri.toString())
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    @Test
    fun sendCameraMessageOpensCamera() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val expectedIntent: Matcher<Intent> = allOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())
            intended(expectedIntent)
        }

        Intents.release()
    }

    @Test
    fun sendGalleryMessageOpensGallery() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val expectedIntent: Matcher<Intent> = allOf(hasAction(Intent.ACTION_PICK))
            onView(withId(R.id.buttonSendImage)).perform(click())
            intended(expectedIntent)
        }

        Intents.release()
    }

    @Test
    fun sendCameraMessageCorrectlyAddsNewMessage() {
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = ApplicationProvider.getApplicationContext<Context>()

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))

            val expectedIntent: Matcher<Intent> = allOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            intending(expectedIntent).respondWith(storeImageAndSetResultStub(instrumentationContext))

            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())

            messagesList.check(
                matches(
                    atPosition(1, withContentDescription("ImageSent"))
                )
            )
        }

        Intents.release()
    }

    @Test
    fun sendGalleryMessageCorrectlyAddsNewMessage() {
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        val context = ApplicationProvider.getApplicationContext<Context>()

        Intents.init()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))

            val expectedIntent: Matcher<Intent> = allOf(hasAction(Intent.ACTION_PICK))
            intending(expectedIntent).respondWith(storeImageAndSetResultStub(instrumentationContext))

            onView(withId(R.id.buttonSendImage)).perform(click())

            messagesList.check(
                matches(
                    atPosition(1, withContentDescription("ImageSent"))
                )
            )
        }

        Intents.release()
    }
}

//TODO: OLD TESTS USING FIREBASE -> COMMENTED TO BE REUSED WHEN FINAL DB IS PROPERLY SETUP
/*
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
    onView(withId(R.id.editName)).perform(typeText("TestSender"))
    closeSoftKeyboard()
    onView(withId(R.id.editMessage)).perform(typeText("TestMessage"))
    closeSoftKeyboard()

    onView(withId(R.id.buttonSendMessage)).perform(click())

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
*/