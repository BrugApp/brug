package com.github.brugapp.brug

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.ui.CHAT_INTENT_KEY
import com.github.brugapp.brug.ui.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.Month

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChatActivityPermissionDeniedTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    private val dummyUser = User("USER1", "Rayan", "Kikou", null, mutableListOf())
    private val dummyDate = DateService.fromLocalDateTime(
        LocalDateTime.of(
            2022, Month.MARCH, 23, 15, 30
        )
    )
    private val conversation = Conversation(
        "USER1USER2",
        dummyUser,
        Item("DummyItem", 0, "DUMMYDESC", false),
        Message(
            dummyUser.getFullName(), dummyDate, "TestMessage"
        )
    )

    private val messagesList = arrayListOf(
        TextMessage(
            dummyUser.getFullName(), dummyDate, "TestMessage"
        )
    )

    // adapted from https://gist.github.com/rocboronat/65b1187a9fca9eabfebb5121d818a3c4
    private val PERMISSIONS_DIALOG_DELAY = 2000L
    private val GRANT_BUTTON_INDEX = 1 // 0 to accept, 1 to deny

    private fun pressOnPermission(permissionNeeded: String?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(
                    permissionNeeded!!
                )
            ) {
                Thread.sleep(PERMISSIONS_DIALOG_DELAY)
                val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                val allowPermissions = device.findObject(
                    UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .index(GRANT_BUTTON_INDEX)
                )
                if (allowPermissions.exists()) {
                    allowPermissions.click()
                }
            }
        } catch (e: UiObjectNotFoundException) {
            println("There is no permissions dialog to interact with")
        }
    }

    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()

    @Before
    fun setUp(){
        Intents.init()
        runBlocking {
            firebaseAuth.signInAnonymously().await()
            UserRepository.addUserFromAccount(
                firebaseAuth.uid!!,
                BrugSignInAccount("CHATACTIVITYLOCATION", "DENIED", "", ""),
                true,
                firestore
            )
        }
    }

    @After
    fun cleanUp(){
        Intents.release()
        if(firebaseAuth.currentUser != null){
            firebaseAuth.signOut()
        }
    }

    private fun hasNeededPermission(permissionNeeded: String): Boolean {
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionStatus = ContextCompat.checkSelfPermission(context, permissionNeeded)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    @Test
    fun locationPermissionDenied() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            Espresso.onView(ViewMatchers.withId(R.id.buttonSendLocalisation))
                .perform(ViewActions.click())

            // deny permissions
            pressOnPermission("Manifest.permission.ACCESS_FINE_LOCATION")

            Espresso.onView(ViewMatchers.withId(R.id.buttonSendLocalisation))
                .perform(ViewActions.click())

            // deny permissions (again)
            pressOnPermission("Manifest.permission.ACCESS_FINE_LOCATION")
        }
    }

    @Test
    fun audioPermissionDenied() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            Espresso.onView(ViewMatchers.withId(R.id.recordButton))
                .perform(ViewActions.click())

            // deny permissions
            pressOnPermission("Manifest.permission.RECORD_AUDIO")
            pressOnPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE")

            Espresso.onView(ViewMatchers.withId(R.id.recordButton))
                .perform(ViewActions.click())

            // deny permissions (again)
            pressOnPermission("Manifest.permission.RECORD_AUDIO")
            pressOnPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE")
        }
    }


}