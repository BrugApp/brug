package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.ui.*
import com.github.brugapp.brug.view_model.ListViewHolder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

private const val TEST_USER_UID = "TwSXfeusCKN95UvlGgY4uvEnXpl2"
private const val INTERLOCUTOR_USER_UID = "qnUozimLdsYaSZXPW1mfGdxkUcR2"
private const val DUMMY_LOST_ITEM = "DummyItemName"

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChatMenuActivityTest {
    @get:Rule
    var rule = HiltAndroidRule(this)


    private fun signInTestUser() {
        runBlocking {
            Firebase.auth.signInWithEmailAndPassword(
                "test@unlost.com",
                "123456").await()

            ConvRepo.addNewConversation(TEST_USER_UID, INTERLOCUTOR_USER_UID, DUMMY_LOST_ITEM)
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        signInTestUser()
        val intent = Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        ActivityScenario.launch<ChatMenuActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
        signOut()
    }

    @Test
    fun changingBottomNavBarMenuToItemsListGoesToActivity() {
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click())
        intended(hasComponent(ItemsMenuActivity::class.java.name))
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

    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
//        Thread.sleep(10000)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(
            UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeLeft(50)

        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual(CHAT_CHECK_TEXT))
    }

    @Test
    fun swipeRightOnItemDeletesItem() { //FAILING -> NEEDS ASYNCHRONY
//        Thread.sleep(10000)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeRight(50)

        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual(CHAT_CHECK_TEXT))
    }

    @Test
    fun clickOnItemGoesToChatActivity() {
//        Thread.sleep(10000)

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val chatList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToClick = chatList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

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