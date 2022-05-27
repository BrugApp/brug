package com.github.brugapp.brug.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.ITEMS_TEST_LIST_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.ItemMapActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class LocationPermissionsAllowedTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    private val ITEMS = arrayListOf(
        Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false),
        Item("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false),
        Item("Car Keys", ItemType.CarKeys.ordinal, "Lamborghini Aventador LP-780-4", false),
        Item("Keys", ItemType.Keys.ordinal, "Home keys", true)
    )

    private val PERMISSIONS_DIALOG_DELAY = 2000L
    private val GRANT_BUTTON_INDEX = 1 // 0 to accept, 1 to accept only once, 2 to reject

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

    @Before
    fun setUp(){
        Intents.init()
    }

    @After
    fun cleanUp(){
        Intents.release()
        BrugDataCache.resetCachedItems()
    }

    private fun hasNeededPermission(permissionNeeded: String): Boolean {
        val context: Context = ApplicationProvider.getApplicationContext()
        val permissionStatus = ContextCompat.checkSelfPermission(context, permissionNeeded)
        return permissionStatus == PackageManager.PERMISSION_GRANTED
    }

    @Test
    fun locationPermissionAllowedDisplaysButtons() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ItemMapActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<Activity>(intent).use {
            Thread.sleep(10000)
            pressOnPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.recenter)).check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )
        }
    }


}