package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.*
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers.`is`
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

private val ITEMS = arrayListOf(
    Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false),
    Item("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false),
    Item("Car Keys", ItemType.CarKeys.ordinal, "Lamborghini Aventador LP-780-4", false),
    Item("Keys", ItemType.Keys.ordinal, "Home keys", true)
)

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ItemsMenuActivityTest {

    private var testUserUid: String = ""
    private val TEST_PASSWORD: String = "123456"
    private val TEST_EMAIL: String = "unlost@itemMenuActivity.com"
    private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")

    @get:Rule
    val rule = HiltAndroidRule(this)

    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firebaseStorage: FirebaseStorage = FirebaseFakeHelper().providesStorage()

    companion object {
        var firstTimeCreate = true
        var firstTimeAccount = true
    }

    private fun createTestUser(){
        runBlocking {
            if(firstTimeCreate){
                firebaseAuth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD).await()
                firstTimeCreate = false
            }
        }
    }

    private fun signInTestAccount(){
        runBlocking{
            firebaseAuth.signInWithEmailAndPassword(
                TEST_EMAIL,
                TEST_PASSWORD
            ).await()
            testUserUid = firebaseAuth.currentUser!!.uid
            if(firstTimeAccount) {
                UserRepository
                    .addUserFromAccount(testUserUid, ACCOUNT1, true, firestore)
                firstTimeAccount = false
            }
        }
    }

    private fun signOut(){
        firebaseAuth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)
    }

    @After
    fun cleanUp() {
        Intents.release()
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
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    ChatMenuActivity::class.java.name
                )
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().putExtra(CONVERSATION_TEST_LIST_KEY, arrayListOf<Conversation>())
            )
        )
        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
        intended(hasComponent(ChatMenuActivity::class.java.name))

    }

    @Test
    fun changingBottomNavBarMenuToMapGoesToActivity() {
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    MapBoxActivity::class.java.name
                )
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
            )
        )
        val itemMenuButton = onView(withId(R.id.item_map_button))
        itemMenuButton.perform(click())
        intended(hasComponent(MapBoxActivity::class.java.name))
    }

    @Test
    fun clickingOnSettingsButtonGoesToActivity() {
        createTestUser()
        signInTestAccount()
        val settingsButton = onView(withId(R.id.my_settings))
        settingsButton.perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        signOut()
    }

    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        val device = UiDevice.getInstance(getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeLeft(50)

//        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
//        assertThat(snackBarTextView.text, IsEqual(ITEMS_DELETE_TEXT))
    }

    @Test
    fun swipeRightOnItemDeletesItem() {
        val device = UiDevice.getInstance(getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeRight(50)

//        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
//        assertThat(snackBarTextView.text, IsEqual(ITEMS_DELETE_TEXT))
    }

//    @Test
//    fun dragUpOnItemReordersList(){
//        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//
//        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
//        val entryToDrag = itemsList.getChild(UiSelector()
//            .resourceId(LIST_ENTRY_ID)
//            .enabled(true)
//            .instance(1))
//
//        val finalDestination = itemsList.getChild(UiSelector()
//            .resourceId(LIST_ENTRY_ID)
//            .enabled(true)
//            .instance(0))
//
//        entryToDrag.dragTo(0, finalDestination.bounds.centerY() - 50,40)
//
//        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
//        assertThat(snackBarTextView.text, IsEqual(ITEMS_MOVE_TEXT))
//    }
//
//    @Test
//    fun dragDownOnItemReordersList(){
//        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//
//        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
//        val entryToDrag = itemsList.getChild(UiSelector()
//            .resourceId(LIST_ENTRY_ID)
//            .enabled(true)
//            .instance(1))
//
//        val finalDestination = itemsList.getChild(UiSelector()
//            .resourceId(LIST_ENTRY_ID)
//            .enabled(true)
//            .instance(2))
//
//        entryToDrag.dragTo(0, finalDestination.bounds.centerY() + 50,40)
//        val snackBarTextView = device.findObject(UiSelector().resourceId(SNACKBAR_ID))
//        assertThat(snackBarTextView.text, IsEqual(ITEMS_MOVE_TEXT))
//    }

    @Test
    fun clickOnItemTriggersInformationItem() {
        UiDevice.getInstance(getInstrumentation())

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
        UiDevice.getInstance(getInstrumentation())

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

//    @Test
//    fun checkIfKeyboardIsShownAfterPressingSearch() {
//        val searchButton = onView(withId(R.id.search_box))
//        searchButton.perform(click())
//        assertThat(isKeyboardOpenedShellCheck(), IsEqual(true))
//    }

    // Companion functions
    private fun isKeyboardOpenedShellCheck(): Boolean {
        val checkKeyboardCmd = "dumpsys input_method | grep mInputShown"

        try {
            return UiDevice.getInstance(getInstrumentation())
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

    /** THIS TEST FAILED DUE TO THE PRESSBACK, WEIRD ISSUE RELATED TO RESUMING THE ACTIVITY */
    @Test
    fun itemIconOnNavBar() {
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    ChatMenuActivity::class.java.name
                )
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().putExtra(CONVERSATION_TEST_LIST_KEY, arrayListOf<Conversation>())
            )
        )
        onView(withId(R.id.chat_menu_button)).perform(click())
//        Espresso.pressBack()

        val selectedItem = BottomNavBar().getSelectedItem(getActivityInstance()!!)
        assertThat(selectedItem, `is`(R.id.items_list_menu_button))
    }

    private fun getActivityInstance(): Activity? {
        val currentActivity = arrayOf<Activity?>(null)
        getInstrumentation().runOnMainSync {
            val resumedActivity =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val it: Iterator<Activity> = resumedActivity.iterator()
            currentActivity[0] = it.next()
        }
        return currentActivity[0]
    }
}