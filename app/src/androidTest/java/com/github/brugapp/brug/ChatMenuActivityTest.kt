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
import com.github.brugapp.brug.ui.ChatMenuActivity
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.github.brugapp.brug.ui.QrCodeScannerActivity
import com.github.brugapp.brug.view_model.ListViewHolder
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val DELETE_CHAT_TEXT = "The conversation has been marked as resolved."

@RunWith(AndroidJUnit4::class)
class ChatMenuActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(ChatMenuActivity::class.java)

    @Before
    fun setUpIntents() {
        Intents.init()
    }

    @After
    fun releaseIntents() {
        Intents.release()
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
    fun clickingOnSettingsButtonTriggersSnackBar() {
        val settingsButton = onView(withId(R.id.my_settings))
        val snackBar =
            onView(withId(com.google.android.material.R.id.snackbar_text))
        settingsButton.perform(click())
        snackBar.check(matches(withText(DUMMY_TEXT)))
    }

    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        val chatList = onView(withId(R.id.chat_listview))
        val snackBar =
            onView(withId(com.google.android.material.R.id.snackbar_text))
        chatList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
                0, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_RIGHT, GeneralLocation.BOTTOM_LEFT,
                    Press.FINGER
                )
            )
        )
        snackBar.check(matches(withText(DELETE_CHAT_TEXT)))
    }

    @Test
    fun swipeRightOnItemDeletesItem() {
        val chatList = onView(withId(R.id.chat_listview))
        val snackBar =
            onView(withId(com.google.android.material.R.id.snackbar_text))
        chatList.perform(
            RecyclerViewActions.actionOnItemAtPosition<ListViewHolder>(
                1, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_LEFT, GeneralLocation.BOTTOM_RIGHT,
                    Press.FINGER
                )
            )
        )
        snackBar.check(matches(withText(DELETE_CHAT_TEXT)))
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