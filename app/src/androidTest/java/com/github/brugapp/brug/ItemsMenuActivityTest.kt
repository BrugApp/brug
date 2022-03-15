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
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val DUMMY_TEXT = "Actual behavior coming soon…"
private const val DELETE_ITEM_TEXT: String = "Item has been deleted."
private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"

private const val LIST_VIEW_ID: String = "$APP_PACKAGE_NAME:id/items_listview"
private const val LIST_ENTRY_ID: String = "$APP_PACKAGE_NAME:id/list_item_title"
private const val SNACKBAR_ID: String = "$APP_PACKAGE_NAME:id/snackbar_text"

@RunWith(AndroidJUnit4::class)
class ItemsMenuActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(ItemsMenuActivity::class.java)

    @Test
    fun changingBottomNavBarMenuToItemsListKeepsFocus() {
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click()).check(matches(isEnabled()))
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
        Intents.init()
        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
        intended(hasComponent(ChatMenuActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun clickingOnSettingsButtonTriggersSnackBar() {
        val settingsButton = onView(withId(R.id.my_settings))
        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
        settingsButton.perform(click())
        snackBar.check(matches(withText(DUMMY_TEXT)))
    }

    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        Intents.init()
        val itemsList = onView(withId(R.id.items_listview))
        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
        itemsList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
                0, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_RIGHT, GeneralLocation.BOTTOM_LEFT,
                    Press.FINGER
                )
            )
        )
        snackBar.check(matches(withText(DELETE_ITEM_TEXT)))
        Intents.release()
    }

    @Test
    fun swipeRightOnItemDeletesItem() {
        val itemsList = onView(withId(R.id.items_listview))
        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
        itemsList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
                1, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_LEFT, GeneralLocation.BOTTOM_RIGHT,
                    Press.FINGER
                )
            )
        )
        snackBar.check(matches(withText(DELETE_ITEM_TEXT)))
    }

    @Test
    fun onClickItemTriggerInformation(){
        Intents.init()
        val itemsList = onView(withId(R.id.items_listview))
        itemsList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
                1, click()
            )
        )
        intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                hasComponent(ItemInformationActivity::class.java.name)
            )
        )
        Intents.release()
    }

    @Test
    fun dragUpOnItemReordersList(){
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToDrag = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(1))

        val finalDestination = itemsList.getChild(UiSelector()
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

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToDrag = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(2))

        val finalDestination = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(3))

        entryToDrag.dragTo(0, finalDestination.bounds.centerY() + 50,40)
        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
        assertThat(snackBarTextView.text, IsEqual("Item has been moved from 2 to 3"))
    }

//    @Test
//    fun clickOnItemTriggersSnackBar() {
//        val itemsList = onView(withId(R.id.items_listview))
//        val snackbar = onView(withId(com.google.android.material.R.id.snackbar_text))
//        itemsList.perform(
//            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
//                1, click()
//            )
//        )
//        snackbar.check(matches(withText(DUMMY_TEXT)))
//    }


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