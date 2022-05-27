package com.github.brugapp.brug.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.ITEMS_TEST_LIST_KEY
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ItemMapActivityNoLocationTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule1: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val permissionRule2: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val ITEMS = arrayListOf(
        Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false),
        Item("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false),
        Item("Car Keys", ItemType.CarKeys.ordinal, "Lamborghini Aventador LP-780-4", false),
        Item("Keys", ItemType.Keys.ordinal, "Home keys", true)
    )

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun mapboxDoesNotCrashWhenNoLocationIsProvided() {

        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, ItemMapActivity::class.java)
        intent.putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
        ActivityScenario.launch<Activity>(intent).use {
            Intents.intended(
                Matchers.allOf(
                    IntentMatchers.toPackage("com.github.brugapp.brug")
                )
            )
        }
    }
}