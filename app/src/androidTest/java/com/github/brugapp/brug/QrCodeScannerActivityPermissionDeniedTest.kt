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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.CHAT_INTENT_KEY
import com.github.brugapp.brug.ui.ChatActivity
import com.github.brugapp.brug.ui.QrCodeScannerActivity
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QrCodeScannerActivityPermissionDeniedTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()

    // adapted from https://gist.github.com/rocboronat/65b1187a9fca9eabfebb5121d818a3c4
    private val PERMISSIONS_DIALOG_DELAY = 2000L
    private var GRANT_BUTTON_INDEX = 1 // 0 to accept, 1 to deny

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

    private var isFirstTime: Boolean = true

    @Before
    fun setUp() {
        Intents.init()

        if(isFirstTime) {
            runBlocking {
                firebaseAuth.createUserWithEmailAndPassword("qrpermissions@test.com","123456").await()
            }
            isFirstTime = false
        }
    }

    @After
    fun cleanUp() {
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
    fun denyPermissions() {
        val userID = "DUMMYUSER"
        val itemID = "DUMMYITEMID"
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword("qrpermissions@test.com", "123456").await()

            UserRepository.addUserFromAccount(
                firebaseAuth.uid!!,
                BrugSignInAccount("USER", "ONE", "", ""),
                true,
                firestore
            )

            UserRepository.addUserFromAccount(
                userID,
                BrugSignInAccount("QRSCAN", "CODETEST", "", ""),
            true,
                firestore
            )

            ItemsRepository.addItemWithItemID(
                Item("DUMMYITEM", 0, "DESC", false),
                itemID,
                userID,
                firestore
            )

        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, QrCodeScannerActivity::class.java)
        ActivityScenario.launch<Activity>(intent).use {
            // deny permissions
            pressOnPermission("Manifest.permission.CAMERA")
            GRANT_BUTTON_INDEX = 2 // NEED TO CHANGE THE INDEX VALUE FOR LOCATION PERMISSIONS
            pressOnPermission("Manifest.permission.ACCESS_FINE_LOCATION")

            val editTextItem = Espresso.onView(ViewMatchers.withId(R.id.edit_message))
            editTextItem.perform(ViewActions.replaceText("${userID}:${itemID}"))
            Espresso.closeSoftKeyboard()

            Espresso.onView(ViewMatchers.withId(R.id.buttonReportItem))
                .perform(ViewActions.click())

            Thread.sleep(1000)
        }
    }
}