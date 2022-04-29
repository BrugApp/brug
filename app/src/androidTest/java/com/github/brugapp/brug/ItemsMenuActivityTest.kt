package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"


private const val LIST_VIEW_ID: String = "$APP_PACKAGE_NAME:id/items_listview"
private const val LIST_ENTRY_ID: String = "$APP_PACKAGE_NAME:id/list_item_title"
private const val SNACKBAR_ID: String = "$APP_PACKAGE_NAME:id/snackbar_text"


private val ITEMS = listOf(
    MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false),
    MyItem("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false),
    MyItem("Car Keys", ItemType.CarKeys.ordinal, "Lamborghini Aventador LP-780-4", false),
    MyItem("Keys", ItemType.Keys.ordinal, "Home keys", true)
)

private const val TEST_USER_UID = "TwSXfeusCKN95UvlGgY4uvEnXpl2"


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ItemsMenuActivityTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    private fun signInTestUser() {
        runBlocking {
            Firebase.auth.signInWithEmailAndPassword(
                "test@unlost.com",
                "123456").await()

            for(item in ITEMS){
                ItemsRepository.addItemToUser(item, TEST_USER_UID)
            }
        }
    }

    private fun wipeAllItemsAndSignOut() {
        runBlocking {
            ItemsRepository.deleteAllUserItems(TEST_USER_UID)
        }
        Firebase.auth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        signInTestUser()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
        wipeAllItemsAndSignOut()
    }

    @Test
    fun changingBottomNavBarMenuToItemsListKeepsFocus() {
        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click()).check(matches(isEnabled()))
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
        intended(hasComponent(ChatMenuActivity::class.java.name))

    }

    @Test
    fun clickingOnSettingsButtonGoesToActivity() {
        val settingsButton = onView(withId(R.id.my_settings))
        settingsButton.perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
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
        assertThat(snackBarTextView.text, IsEqual(ITEMS_DELETE_TEXT))
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
        assertThat(snackBarTextView.text, IsEqual(ITEMS_DELETE_TEXT))
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
        assertThat(snackBarTextView.text, IsEqual(ITEMS_MOVE_TEXT))
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
        assertThat(snackBarTextView.text, IsEqual(ITEMS_MOVE_TEXT))
    }

    @Test
    fun clickOnItemTriggersInformationItem() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val chatList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToClick = chatList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToClick.click()

        intended(hasComponent(ItemInformationActivity::class.java.name))
    }

    @Test
    fun clickOnButtonTriggersQrCode() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val chatList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToClick = chatList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToClick.click()

        val button = onView(withId(R.id.qrGen))
        button.perform(click())
        intended(hasComponent(QrCodeShowActivity::class.java.name))
    }

    @Test
    fun checkIfKeyboardIsShownAfterPressingSearch() {
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

    @Test
    fun goToAddItemPageOnAddButton() {
        onView(withId(R.id.add_new_item_button)).perform(click())
        // Verify that the app goes to the Item List activity if the User enters valid info for his/her new item.
        intended(
            allOf(
                toPackage(APP_PACKAGE_NAME),
                hasComponent(AddItemActivity::class.java.name)
            )
        )
    }
}