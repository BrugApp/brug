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
import com.github.brugapp.brug.model.MyItem
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
    private val item = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        .setLastLocation(6.61,46.51)


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
        val ouchy = "Av. Emile-Henri-Jaques-Dalcroze 7, 1007 Lausanne, Switzerland"
        onView(withId(R.id.item_last_location)).check(matches(withText(ouchy)))
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
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LocationTest {
    private val myItem = MyItem("IPhone", ItemType.Phone.ordinal, "IPhone 13", false)

    @get:Rule
    var rule = HiltAndroidRule(this)

    fun setUp(item:MyItem) {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ItemInformationActivity::class.java)
        intent.putExtra(ITEM_INTENT_KEY, item)
        ActivityScenario.launch<ItemInformationActivity>(intent)
    }

    @Test
    fun correctLocationDisplayed() {
        setUp(myItem)
        onView(withId(R.id.item_last_location)).check(matches(withText("Not available")))
    }

    @Test
    fun correctLocationDisplayedAfterBadLocationUpdate() {
        myItem.setLastLocation(0.0,0.0) // set bad location
        setUp(myItem)
        onView(withId(R.id.item_last_location)).check(matches(withText("Not available")))
    }

    @Test
    fun locationDoesntExist(){
        myItem.setLastLocation(1000.0,10000.0) // set bad location (out of map) => exception
        setUp(myItem)
        onView(withId(R.id.item_last_location)).check(matches(withText("Not available")))
    }
}
