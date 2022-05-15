package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.ItemInformationActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"
private const val TOGGLE_SWITCH_ID: String = "$APP_PACKAGE_NAME:id/isLostSwitch"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ItemInformationActivityTest {
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val str = "no information yet"
    private val item = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ItemInformationActivity::class.java)
        intent.putExtra(ITEM_INTENT_KEY, item)
        ActivityScenario.launch<ItemInformationActivity>(intent)
    }

    @Test
    fun correctTypeDisplayed() {
        onView(withId(R.id.item_name)).check(matches(withText("Phone")))
        onView(withId(R.id.tv_name)).check(matches(withText("Phone")))
    }

    @Test
    fun correctDescriptionDisplayed() {
        onView(withId(R.id.item_description)).check(matches(withText("Samsung Galaxy S22")))
    }

    @Test
    fun noLocationAndOwnerAndDateYet() {
        onView(withId(R.id.item_last_location)).check(matches(withText(str)))
        onView(withId(R.id.item_owner)).check(matches(withText(str)))
    }

    @Test
    fun declaredItemAsLost() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                "test@unlost.com",
                "123456"
            )
        }
        assertThat(
            item.isLost(),
            CoreMatchers.`is`(false)
        )

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val switchVal = device.findObject(UiSelector().resourceId(TOGGLE_SWITCH_ID)).click()
        assertThat(switchVal, IsEqual(true))
        firebaseAuth.signOut()
    }
}