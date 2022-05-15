package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"

private const val LIST_VIEW_ID: String = "$APP_PACKAGE_NAME:id/chat_listview"
private const val LIST_ENTRY_ID: String = "$APP_PACKAGE_NAME:id/chat_entry_title"
private const val SNACKBAR_ID: String = "$APP_PACKAGE_NAME:id/snackbar_text"

private const val TEST_USER_EMAIL = "test@convMenu.com"
private const val PASSWORD = "123456"

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChatMenuActivityTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    private val firebaseAuth = FirebaseFakeHelper().providesAuth()

    companion object {
        var testFirstTime = true
    }


    private fun signInTestUser() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                TEST_USER_EMAIL,
                PASSWORD
            ).await()
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
    }

    private fun createTestUser() {
        runBlocking {
            if (testFirstTime) {
                firebaseAuth.createUserWithEmailAndPassword(
                    TEST_USER_EMAIL,
                    PASSWORD
                ).await()
                testFirstTime = false
            }
        }
    }

    private val convUser = User("DUMMYUID", "Firstname", "Lastname", null, mutableListOf())
    private val convList = arrayListOf(
        Conversation("CONVID", convUser, "LOSTITEM", null),
        Conversation("CONVID", convUser, "LOSTITEM", null),
        Conversation("CONVID", convUser, "LOSTITEM", null),
        Conversation("CONVID", convUser, "LOSTITEM", null)
    )

    @Before
    fun setUp() {
        Intents.init()
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
    }



    @After
    fun cleanUp() {
        Intents.release()
//        signOut()
    }

    //TODO: USE CACHE FOR ITEMS HERE !
    @Test
    fun changingBottomNavBarMenuToItemsListGoesToActivity() {
        createTestUser()
        signInTestUser()
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click())
        intended(hasComponent(ItemsMenuActivity::class.java.name))
        signOut()
    }

    @Test
    fun changingBottomNavBarMenuToQRScanGoesToActivity() {
        val scanQRMenuButton = onView(withId(R.id.qr_scan_menu_button))
        scanQRMenuButton.perform(click())
        intended(hasComponent(QrCodeScannerActivity::class.java.name))
    }

    @Test
    fun changingBottomNavBarMenuToChatGoesToActivity() {
        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun clickingOnSettingsButtonGoesToActivity() {
        val settingsButton = onView(withId(R.id.my_settings))
        settingsButton.perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
    }

    // Always fails in CI after merge with main
//    @Test
//    fun changingBottomNavBarMenuToMapGoesToActivity() {
//        val itemMapButton = onView(withId(R.id.item_map_button))
//        itemMapButton.perform(click())
//        Thread.sleep(10000)
//        intended(hasComponent(MapBoxActivity::class.java.name))
//    }


    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(
            UiSelector()
                .resourceId(LIST_ENTRY_ID)
                .enabled(true)
                .instance(0)
        )

        entryToSwipe.swipeLeft(50)

//        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
//        assertThat(snackBarTextView.text, IsEqual(CHAT_CHECK_TEXT))
    }



    @Test
    fun swipeRightOnItemDeletesItem() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(
            UiSelector()
                .resourceId(LIST_ENTRY_ID)
                .enabled(true)
                .instance(0)
        )

        entryToSwipe.swipeRight(50)

//        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
//        assertThat(snackBarTextView.text, IsEqual(CHAT_CHECK_TEXT))
    }

    //TODO: Test clicking on item goes to chat
    @Test
    fun clickOnItemGoesToChatActivity() {
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    ChatActivity::class.java.name
                )
            )
        ).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(MESSAGE_TEST_LIST_KEY, arrayListOf<Message>())
                )
            )

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val chatList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToClick = chatList.getChild(
            UiSelector()
                .resourceId(LIST_ENTRY_ID)
                .enabled(true)
                .instance(0)
        )

        entryToClick.click()

        intended(hasComponent(ChatActivity::class.java.name))
    }


    @Test
    fun checkifKeyboardIsShownAfterPressingSearch() {
        val searchButton = onView(withId(R.id.search_box))
        searchButton.perform(click())
        assertThat(isKeyboardOpenedShellCheck(), IsEqual(true))
    }


    // Companion functions
    private fun isKeyboardOpenedShellCheck(): Boolean {
        val checkKeyboardCmd = "dumpsys input_method | grep mInputShown"

        try {
            return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .executeShellCommand(checkKeyboardCmd).contains("mInputShown=true")
        } catch (e: IOException) {
            throw RuntimeException("Keyboard check failed", e)
        }
    }

}