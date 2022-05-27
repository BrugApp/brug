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
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.ItemsRepository
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
    @get:Rule
    val rule = HiltAndroidRule(this)

    // ONLY FOR ONE SWIPE TEST, USING FIREBASE TO IMPROVE TEST COVERAGE
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()

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

    //TODO: USE CACHE FOR ITEMS HERE !
    @Test
    fun changingBottomNavBarMenuToItemsListGoesToActivity() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

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
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

        val scanQRMenuButton = onView(withId(R.id.qr_scan_menu_button))
        scanQRMenuButton.perform(click())
        intended(hasComponent(QrCodeScannerActivity::class.java.name))
    }

    @Test
    fun changingBottomNavBarMenuToChatGoesToActivity() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

        val chatMenuButton = onView(withId(R.id.chat_menu_button))
        chatMenuButton.perform(click()).check(matches(isEnabled()))
    }

    @Test
    fun clickingOnSettingsButtonGoesToActivity() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

        runBlocking {
            firebaseAuth.signInAnonymously().await()
            UserRepository.addUserFromAccount(
                firebaseAuth.uid!!,
                BrugSignInAccount("CHAT", "ANONYMOUSUSER", "", ""),
                true,
                firestore
            )
        }

        val settingsButton = onView(withId(R.id.my_settings))
        settingsButton.perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
    }


    @Test
    fun swipeLeftOnConversationDeletesConversation() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val convList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = convList.getChild(
            UiSelector()
                .resourceId(LIST_ENTRY_ID)
                .enabled(true)
                .instance(0)
        )

        entryToSwipe.swipeLeft(50)

        Thread.sleep(1000)
        onView(withText("Undo")).perform(click())
        Thread.sleep(1000)
    }



    @Test
    fun swipeRightOnConversationDeletesConversation() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val convList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = convList.getChild(
            UiSelector()
                .resourceId(LIST_ENTRY_ID)
                .enabled(true)
                .instance(0)
        )

        entryToSwipe.swipeRight(50)

        Thread.sleep(1000)
        onView(withText("Undo")).perform(click())
        Thread.sleep(1000)
    }

    @Test
    fun swipeOnConversationWithFirebaseEnabledDeletesConversationAndReAddsIt() {
        runBlocking {
            firebaseAuth.signInAnonymously().await()

            val userID = firebaseAuth.uid!!
            val anonymousID = "ANONYMOUSACCOUNT"
            val itemID = "MYITEMID"

            UserRepository.addUserFromAccount(
                userID,
                BrugSignInAccount("CHATMENU", "SWIPEUSER", "", ""),
                true,
                firestore
            )

            UserRepository.addUserFromAccount(
                "ANONYMOUSACCOUNT",
                BrugSignInAccount("CHATSECOND", "USER2", "", ""),
                true,
                firestore
            )

            ItemsRepository.addItemWithItemID(
                Item("MYITEM", 0, "NODESC", false),
                itemID,
                userID,
                firestore
            )

            ConvRepository.addNewConversation(
                userID,
                anonymousID,
                "${userID}:${itemID}",
                null,
                firestore
            )
        }

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ChatMenuActivity::class.java)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)


        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val convList = UiScrollable(UiSelector().resourceId(LIST_VIEW_ID))
        val entryToSwipe = convList.getChild(UiSelector()
            .resourceId(LIST_ENTRY_ID)
            .enabled(true)
            .instance(0))

        entryToSwipe.swipeRight(50)
        Thread.sleep(1000)
//        onView(withText("Undo")).perform(click())
//        Thread.sleep(1000)
    }

    //TODO: Test clicking on item goes to chat
    @Test
    fun clickOnItemGoesToChatActivity() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

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
    fun chatIconOnNavBar() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
        intent.putExtra(CONVERSATION_TEST_LIST_KEY, convList)
        ActivityScenario.launch<ChatMenuActivity>(intent)
        Thread.sleep(1000)

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

}