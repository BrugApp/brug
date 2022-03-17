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
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsEqual
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


private const val DUMMY_TEXT = "Actual behavior coming soon..."

@RunWith(AndroidJUnit4::class)
class ItemsMenuActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(ItemsMenuActivity::class.java)

    @Test
    fun changingBottomNavBarMenuToItemsListChangesFocus() {
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun changingBottomNavBarMenuToQRScanChangesFocus() {
        val scanQRMenuButton = onView(withId(R.id.qr_scan_menu_button))
        scanQRMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun changingBottomNavBarMenuToChatChangesFocus() {
        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun clickingOnSettingsButtonTriggersSnackBar() {
        val settingsButton = onView(withId(R.id.my_settings))
        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
        settingsButton.perform(click())
        snackBar.check(matches(withText(DUMMY_TEXT)))
    }

    /* THESE NEED A CONFLICTING ESPRESSO DEPENDENCY */
    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        val itemsList = onView(withId(R.id.items_listview))
        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
        itemsList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ItemsCustomAdapter.ViewHolder>(
                0, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_RIGHT, GeneralLocation.BOTTOM_LEFT,
                    Press.FINGER
                )
            )
        )
        snackBar.check(matches(withText("Item \"Phone\" is deleted")))
    }

    @Test
    fun swipeRightOnItemDeletesItem() {
        val itemsList = onView(withId(R.id.items_listview))
        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
        itemsList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ItemsCustomAdapter.ViewHolder>(
                1, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_LEFT, GeneralLocation.BOTTOM_RIGHT,
                    Press.FINGER
                )
            )
        )
        snackBar.check(matches(withText("Item \"Wallet\" is deleted")))
    }

//    @Test
//    fun dragUpOnItemReordersList(){
//        val itemsList = onView(withId(R.id.items_listview))
//        val snackBar = onView(withId(com.google.android.material.R.id.snackbar_text))
//        itemsList.perform(
//            RecyclerViewActions.actionOnItemAtPosition<ItemsCustomAdapter.ViewHolder>(
//
//                3, longClick().apply {
//                    RecyclerViewActions.actionOnItemAtPosition<ItemsCustomAdapter.ViewHolder>(
//                        3, ViewPagerActions.scrollToFirst()
//                    )
//                }
//            )
//        )
//
//        snackBar.check(matches(withText("Item \"BMW Key\" has been moved from 3 to 2")))
//    }
//
//    @Test
//    fun dragDownOnItemReordersList(){
//        val itemsList = onView(withId(R.id.items_listview))
//
//    }


    @Test
    fun checkifKeyboardIsShownAfterPressingSearch() {
        val searchButton = onView(withId(R.id.search_items))
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

    @Test
    fun goToAddItemPageOnAddButton() {
        Intents.init()
        onView(withId(R.id.add_new_item_button)).perform(click())

        // Verify that the app goes to the Item List activity if the User enters valid info for his/her new item.
        Intents.intended(
            allOf(
                toPackage("com.github.brugapp.brug"),
                hasComponent(AddItemActivity::class.java.name)
            )
        )
        Intents.release()
    }
}