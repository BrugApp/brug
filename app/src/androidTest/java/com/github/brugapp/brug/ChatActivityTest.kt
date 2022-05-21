package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Process.SYSTEM_UID
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService.Companion.fromLocalDateTime
import com.github.brugapp.brug.ui.CHAT_INTENT_KEY
import com.github.brugapp.brug.ui.ChatActivity
import com.github.brugapp.brug.ui.MapBoxActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.Month
import java.util.*


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatActivityTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @get:Rule
    val permissionRuleCoarseLocation: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val permissionRuleFineLocation: GrantPermissionRule =
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

    private val dummyUser = MyUser("USER1", "Rayan", "Kikou", null, mutableListOf())
    private val dummyDate = fromLocalDateTime(
        LocalDateTime.of(
            2022, Month.MARCH, 23, 15, 30
        )
    )
    private val conversation = Conversation(
        "USER1USER2",
        dummyUser,
        MyItem("DummyItem", 0, "DUMMYDESC", false),
        Message(
            dummyUser.getFullName(), dummyDate, "TestMessage"
        )
    )

    private val messagesList = arrayListOf(
        TextMessage(
            dummyUser.getFullName(), dummyDate, "TestMessage"
        )
    )

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun chatViewCorrectlyGetsConversationInfos() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            messagesList.check(
                matches(
                    atPosition(
                        0,
                        hasDescendant(withText("TestMessage"))
                    )
                )
            )
        }
    }

    @Test
    fun sendMessageCorrectlyAddsNewMessage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val newMessageText = "Test sending new messages"

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
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

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))
            onView(withId(R.id.buttonSendLocalisation)).perform(click())

            // wait for the message to be uploaded
            Thread.sleep(5000)

            messagesList.check(
                matches(
                    atPosition(1, hasDescendant(withContentDescription("location")))
                )
            )
        }
    }

    @Test
    fun localisationButtonGoneAfterRecord() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
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
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
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
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
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
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
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
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        val message = "Test text"

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.editMessage)).perform(typeText(message))
            onView(withId(R.id.buttonSendMessage)).perform(click())
            onView(withId(R.id.recordButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun initialChatSetupAfterAudio() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        val message = "Test text"

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.recordButton)).perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.buttonSendAudio)).perform(click())
            onView(withId(R.id.recordButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun sendCameraMessageOpensCamera() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val expectedIntent: Matcher<Intent> = allOf(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())
            intended(expectedIntent)
        }
    }

    @Test
    fun sendGalleryMessageOpensGallery() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val expectedIntent: Matcher<Intent> = allOf(hasAction(Intent.ACTION_PICK))
            onView(withId(R.id.buttonSendImage)).perform(click())
            intended(expectedIntent)
        }
    }

    @Test
    fun sendCameraMessageCorrectlyAddsNewMessage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))

            val expectedIntent: Matcher<Intent> = hasAction(MediaStore.ACTION_IMAGE_CAPTURE)
            intending(expectedIntent).respondWith(storeImageAndSetResultStub())

            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())

            messagesList.check(
                matches(
                    atPosition(1, withContentDescription("ImageSent"))
                )
            )
        }
    }

    @Test
    fun sendGalleryMessageCorrectlyAddsNewMessage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val messagesList = onView(withId(R.id.messagesList))

            val expectedIntent: Matcher<Intent> = hasAction(Intent.ACTION_PICK)
            intending(expectedIntent).respondWith(storeImageAndSetResultStub())

            onView(withId(R.id.buttonSendImage)).perform(click())

            messagesList.check(
                matches(
                    atPosition(1, withContentDescription("ImageSent"))
                )
            )

        }
    }

    @Test
    fun pressImageOpensFullScreenActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            val cameraIntent: Matcher<Intent> = hasAction(MediaStore.ACTION_IMAGE_CAPTURE)
            intending(cameraIntent).respondWith(storeImageAndSetResultStub())

            onView(withId(R.id.buttonSendImagePerCamera)).perform(click())

            val messagesList = onView(withId(R.id.messagesList))
            messagesList.perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withContentDescription("ImageSent")),
                    click()
                )
            )

            val expectedIntent: Matcher<Intent> = anyOf(
                hasComponent(FullScreenImageTest::class.java.name),
                hasExtraWithKey("messageUrl")
            )
            intended(expectedIntent)
        }
    }

    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()

    @Test
    fun pressLocationOpensMapActivity() {
        runBlocking {
            firebaseAuth.createUserWithEmailAndPassword("goat@efgh.com", "123456").await()
            firebaseAuth.signInWithEmailAndPassword("goat@efgh.com", "123456").await()
        }
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.buttonSendLocalisation)).perform(click())

            // wait for the message to be uploaded
            Thread.sleep(5000)

            val messagesList = onView(withId(R.id.messagesList))
            messagesList.perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withContentDescription("location")),
                    click()
                )
            )

            Thread.sleep(10000)
            
            intended(allOf(
                hasComponent(MapBoxActivity::class.java.name)))

            firebaseAuth.signOut()
        }
    }

    // DISPLAY Tests
    @Test
    fun checkIfFieldsArePresent() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)

        }

        ActivityScenario.launch<Activity>(intent).use {
            onView(withId(R.id.editMessage)).check(matches(withHint("Message")))
            onView(withId(R.id.buttonSendImage)).check(matches(withContentDescription("Send image")))
            onView(withId(R.id.buttonSendImagePerCamera)).check(matches(withContentDescription("Send image via camera")))
            onView(withId(R.id.buttonSendLocalisation)).check(matches(withContentDescription("Send localisation")))
            onView(withId(R.id.buttonSendAudio)).check(matches(withContentDescription("sendAudio")))
        }
    }

    // HELPER FUNCTIONS
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

    // Helper function to create and stub an image
    private fun storeImageAndSetResultStub(): Instrumentation.ActivityResult {
        // create bitmap
        // below is a base64 blue image
        val encodedImage =
            "iVBORw0KGgoAAAANSUhEUgAAAKQAAACZCAYAAAChUZEyAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAG0SURBVHhe7dIxAcAgEMDALx4rqKKqDxZEZLhbYiDP+/17IGLdQoIhSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSEJmDnORA7zZz2YFAAAAAElFTkSuQmCC"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // create file
        val imageFile = File.createTempFile("dummyIMG", ".jpg")

        // store to outputstream
        val outputStream = FileOutputStream(imageFile)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        // return with uri as data
        val resultData = Intent()
        resultData.putExtra(PIC_ATTACHMENT_INTENT_KEY, Uri.fromFile(imageFile).toString())
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }
}
