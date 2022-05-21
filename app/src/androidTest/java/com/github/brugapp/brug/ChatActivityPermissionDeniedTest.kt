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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.ui.CHAT_INTENT_KEY
import com.github.brugapp.brug.ui.ChatActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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

    private val dummyUser = MyUser("USER1", "Rayan", "Kikou", null, mutableListOf())
    private val dummyDate = DateService.fromLocalDateTime(
        LocalDateTime.of(
            2022, Month.MARCH, 23, 15, 30
        )
    )
    private val conversation = Conversation(
        "USER1USER2",
        dummyUser,
        "DummyItem",
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
    fun cameraPermissionDenied() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(CHAT_INTENT_KEY, conversation)
            putExtra(MESSAGE_TEST_LIST_KEY, messagesList)
        }

        ActivityScenario.launch<Activity>(intent).use {
            Espresso.onView(ViewMatchers.withId(R.id.buttonSendImagePerCamera))
                .perform(ViewActions.click())

            // deny permissions
            pressOnPermission("Manifest.permission.CAMERA")

            Espresso.onView(ViewMatchers.withId(R.id.buttonSendImagePerCamera))
                .perform(ViewActions.click())

            // deny permissions (again)
            pressOnPermission("Manifest.permission.CAMERA")
        }
    }
}