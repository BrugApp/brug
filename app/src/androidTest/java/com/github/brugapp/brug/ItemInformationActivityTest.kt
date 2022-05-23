package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import kotlinx.coroutines.tasks.await
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
    private val item = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)

    private val str = "no information yet"

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        item.setLastLocation(6.61,46.51)
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
        runBlocking {
            firebaseAuth.createUserWithEmailAndPassword("abcd@efgh.com", "123456").await()
            firebaseAuth.signInWithEmailAndPassword("abcd@efgh.com", "123456").await()
        }
        val ouchy = "Av. Emile-Henri-Jaques-Dalcroze 7, 1007 Lausanne, Switzerland"
        Thread.sleep(1000)
        onView(withId(R.id.item_last_location)).check(matches(withText(ouchy)))
        onView(withId(R.id.item_last_location)).perform(click())
        firebaseAuth.signOut()
    }

    @Test
    fun declaredItemAsLost() {
        runBlocking {
            firebaseAuth.createUserWithEmailAndPassword("test@unlost.com", "123456").await()
            firebaseAuth.signInWithEmailAndPassword(
                "test@unlost.com",
                "123456"
            ).await()
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

