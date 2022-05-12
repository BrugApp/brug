package com.github.brugapp.brug

import android.content.Intent
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.ui.*
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
import java.time.LocalDateTime
import java.time.Month

private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"

private const val LIST_VIEW_ID: String = "$APP_PACKAGE_NAME:id/chat_listview"
private const val LIST_ENTRY_ID: String = "$APP_PACKAGE_NAME:id/chat_entry_title"
private const val SNACKBAR_ID: String = "$APP_PACKAGE_NAME:id/snackbar_text"

private var test_user_uid = ""
private var interlocutor_uid = ""
private const val DUMMY_LOST_ITEM = "DummyItemName"
private const val INTERLOCUTOR_EMAIL = "Interlocuteur@lespommes.ch"
private const val TEST_USER_EMAIL = "test@convMenu.com"
private const val PASSWORD = "123456"
private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")
private val ACCOUNT2 = BrugSignInAccount("Hamza", "Hassoune", "", "")

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ChatMenuActivityTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    private val firebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseStorage = FirebaseFakeHelper().providesStorage()
    private val dummyDate = DateService.fromLocalDateTime(
        LocalDateTime.of(
            2022, Month.MARCH, 23, 15, 30
        )
    )
    companion object {
        var testFirstTime = true
        var interlocutorFirstTime = true
    }


    private fun signInTestUser() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                TEST_USER_EMAIL,
                PASSWORD
            ).await()
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
    }

    private fun createInterlocutorUser() {
        runBlocking {
            if (interlocutorFirstTime) {
                firebaseAuth.createUserWithEmailAndPassword(
                    INTERLOCUTOR_EMAIL,
                    PASSWORD
                ).await()
                interlocutorFirstTime = false
            }
        }
    }
    private fun signInInterlocutor() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(INTERLOCUTOR_EMAIL, PASSWORD).await()
        }
    }

    private fun getUidInterlocutor() {
        runBlocking {
            interlocutor_uid = firebaseAuth.currentUser?.uid!!
        }
    }
    private fun createTestUser() {
        runBlocking {
            if (testFirstTime) {
                firebaseAuth.createUserWithEmailAndPassword(
                    TEST_USER_EMAIL,
                    PASSWORD
                ).await()
                testFirstTime = false
            }
        }
    }

    private fun getUidTestUser() {
        runBlocking {
            test_user_uid = firebaseAuth.currentUser?.uid!!
        }
    }
    private fun createConv() {
        runBlocking {
            UserRepository
                .addUserFromAccount(test_user_uid, ACCOUNT1, true, firestore)
            UserRepository
                .addUserFromAccount(interlocutor_uid, ACCOUNT2, true, firestore)
            val result = ConvRepository.addNewConversation(
                test_user_uid,
                interlocutor_uid,
                DUMMY_LOST_ITEM,
                firestore
            )
            val convID = "$test_user_uid$interlocutor_uid"
            val message = Message(ACCOUNT1.firstName, dummyDate, "Bonjour")
            MessageRepository.addMessageToConv(
                message,
                test_user_uid,
                convID,
                firestore,
                firebaseAuth,
                firebaseStorage
            )
//            val convs = ConvRepository.getUserConvFromUID(
//                test_user_uid,
//                firestore,
//                firebaseAuth,
//                firebaseStorage
//            )
            //NO MESSAGE SO THERE IS AN ERROR
            Log.d("createConv", result.onError.toString())
//            Log.d("convs list", convs.toString())
        }

    }

    @Before
    fun setUp() {
        Intents.init()
        createInterlocutorUser()
        signInInterlocutor()
        getUidInterlocutor()
        signOut()
        createTestUser()
        signInTestUser()
        getUidTestUser()
        createConv()
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ChatMenuActivity::class.java)
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