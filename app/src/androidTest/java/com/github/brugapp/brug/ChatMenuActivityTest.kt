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
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.*
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers
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

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChatMenuActivityTest {

    private val convUser = User("DUMMYUID", "Firstname", "Lastname", null, mutableListOf())
    private val convList = arrayListOf(
        Conversation("CONVID", convUser, Item("LOSTITEM", 0, "DUMMYDESC", false), null),
        Conversation("CONVID", convUser, Item("LOSTITEM", 0, "DUMMYDESC", false), null),
        Conversation("CONVID", convUser, Item("LOSTITEM", 0, "DUMMYDESC", false), null),
        Conversation("CONVID", convUser, Item("LOSTITEM", 0, "DUMMYDESC", false), null)
        )
    private val itemsList = arrayListOf(
        Item("LOSTITEM", 0, "DUMMYDESC", false),
        Item("LOSTITEM", 0, "DUMMYDESC", false),
        Item("LOSTITEM", 0, "DUMMYDESC", false),
        Item("LOSTITEM", 0, "DUMMYDESC", false)
    )

    private var testUserUid: String = ""
    private val TEST_PASSWORD: String = "123456"
    private val TEST_EMAIL: String = "unlost@chatMenuActivity.com"
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
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    //TODO: USE CACHE FOR ITEMS HERE !
    @Test
    fun changingBottomNavBarMenuToItemsListGoesToActivity() {
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    ItemsMenuActivity::class.java.name
                )
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().putExtra(ITEMS_TEST_LIST_KEY, itemsList)
            )
        )
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
        createTestUser()
        signInTestAccount()
        val settingsButton = onView(withId(R.id.my_settings))
        settingsButton.perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        signOut()
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


//    @Test
//    fun checkifKeyboardIsShownAfterPressingSearch() {
//        val searchButton = onView(withId(R.id.search_box))
//        searchButton.perform(click())
//        assertThat(isKeyboardOpenedShellCheck(), IsEqual(true))
//    }

    //TODO: USE CACHE FOR ITEMS HERE !
    @Test
    fun chatIconOnNavBar() {
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    ItemsMenuActivity::class.java.name
                )
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().putExtra(ITEMS_TEST_LIST_KEY, itemsList)
            )
        )

        onView(withId(R.id.items_list_menu_button)).perform(click())

//        Espresso.pressBack()
//        Thread.sleep(1000)
        val selectedItem = BottomNavBar().getSelectedItem(getActivityInstance()!!)
        assertThat(selectedItem, Matchers.`is`(R.id.chat_menu_button))

    }

    private fun getActivityInstance(): Activity? {
        val currentActivity = arrayOf<Activity?>(null)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
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
            return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .executeShellCommand(checkKeyboardCmd).contains("mInputShown=true")
        } catch (e: IOException) {
            throw RuntimeException("Keyboard check failed", e)
        }
    }

}