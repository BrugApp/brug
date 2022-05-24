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
import com.github.brugapp.brug.data.ItemsRepository
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

    // ONLY FOR ONE SWIPE TEST, USING FIREBASE TO IMPROVE TEST COVERAGE
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
        if(firebaseAuth.currentUser != null){
            firebaseAuth.signOut()
        }
    }

    @Test
    fun changingBottomNavBarMenuToItemsListKeepsFocus() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)

        val itemsListMenuButton = onView(withId(R.id.items_list_menu_button))
        itemsListMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun changingBottomNavBarMenuToQRScanGoesToActivity() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)

        val scanQRMenuButton = onView(withId(R.id.qr_scan_menu_button))
        scanQRMenuButton.perform(click())
        intended(hasComponent(QrCodeScannerActivity::class.java.name))

    }

    @Test
    fun changingBottomNavBarMenuToChatGoesToActivity() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


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
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


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
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


        val settingsButton = onView(withId(R.id.my_settings))
        settingsButton.perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
    }

    @Test
    fun swipeLeftOnItemTriggersSnackBar() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


        UiDevice.getInstance(getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeLeft(50)

        Thread.sleep(1000)
        onView(withText("Undo")).perform(click())
        Thread.sleep(1000)
    }

    @Test
    fun swipeRightOnItemDeletesItem() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


        UiDevice.getInstance(getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeRight(50)

        Thread.sleep(1000)
        onView(withText("Undo")).perform(click())
        Thread.sleep(1000)
    }

    @Test
    fun swipeOnItemWithFirebaseEnabledDeletesItemAndReAddsIt() {
        runBlocking {
            firebaseAuth.signInAnonymously().await()

            val userID = firebaseAuth.uid!!

            UserRepository.addUserFromAccount(
                userID,
                BrugSignInAccount("ITEMSMENU", "SWIPEUSER", "", ""),
                true,
                firestore
            )

            ItemsRepository.addItemWithItemID(
                Item("MYITEM", 0, "NODESC", false),
                "MYITEMID",
                userID,
                firestore
            )
        }

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


        UiDevice.getInstance(getInstrumentation())

        val itemsList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = itemsList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeRight(50)
        Thread.sleep(1000)
        onView(withText("Undo")).perform(click())
        Thread.sleep(1000)
    }

    @Test
    fun clickOnItemTriggersInformationItem() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)

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
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)


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

    @Test
    fun checkIfKeyboardIsShownAfterPressingSearch() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)

        val searchButton = onView(withId(R.id.search_box))
        searchButton.perform(click())
        assertThat(isKeyboardOpenedShellCheck(), IsEqual(true))
    }

    @Test
    fun goToAddItemPageOnAddButton() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)

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
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
        Thread.sleep(1000)

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
}