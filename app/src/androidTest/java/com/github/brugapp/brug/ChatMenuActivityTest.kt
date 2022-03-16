package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
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
import org.hamcrest.core.IsEqual
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val DELETE_CHAT_TEXT = "Chat feed has been deleted."
private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"

private const val LIST_VIEW_ID: String = "$APP_PACKAGE_NAME:id/chat_listview"
private const val LIST_ENTRY_ID: String = "$APP_PACKAGE_NAME:id/chat_entry_title"
private const val SNACKBAR_ID: String = "$APP_PACKAGE_NAME:id/snackbar_text"

@RunWith(AndroidJUnit4::class)
class ChatMenuActivityTest {

    @get:Rule
    val testRule = ActivityScenarioRule(ChatMenuActivity::class.java)

    @Test
    fun changingBottomNavBarMenuToItemsListGoesToActivity() {
        Intents.init()
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click())
        intended(hasComponent(ItemsMenuActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun changingBottomNavBarMenuToQRScanGoesToActivity() {
        Intents.init()
        val scanQRMenuButton = onView(withId(R.id.qr_scan_menu_button))
        scanQRMenuButton.perform(click())
        intended(hasComponent(QrCodeScannerActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun changingBottomNavBarMenuToChatGoesToActivity() {
        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun clickingOnSettingsButtonTriggersSnackBar() {
        val settingsButton = onView(withId(R.id.my_settings))
        val snackBar =
            onView(withId(com.google.android.material.R.id.snackbar_text))
        settingsButton.perform(click())
        snackBar.check(matches(withText(DUMMY_TEXT)))
    }

    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeLeft(50)

        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual(DELETE_CHAT_TEXT))
    }

    @Test
    fun swipeRightOnItemDeletesItem() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeRight(50)

        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual(DELETE_CHAT_TEXT))
    }

    @Test
    fun dragUpOnItemReordersList(){
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val chatList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToDrag = chatList.getChild(
            UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(1))

        val finalDestination = chatList.getChild(
            UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToDrag.dragTo(0, finalDestination.bounds.centerY() - 50,40)

        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual("Item has been moved from 1 to 0"))
    }

    @Test
    fun dragDownOnItemReordersList(){
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val chatList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToDrag = chatList.getChild(
            UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(2))

        val finalDestination = chatList.getChild(
            UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(3))

        entryToDrag.dragTo(0, finalDestination.bounds.centerY() + 50,40)
        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual("Item has been moved from 2 to 3"))
    }

    @Test
    fun clickOnItemTriggersSnackBar() {
        val chatList = onView(withId(R.id.chat_listview))
        val snackbar = onView(withId(com.google.android.material.R.id.snackbar_text))
        chatList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
                1, click()
            )
        )
        snackbar.check(matches(withText(DUMMY_TEXT)))
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