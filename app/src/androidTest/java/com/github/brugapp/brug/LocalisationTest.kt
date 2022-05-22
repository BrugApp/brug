package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.ItemInformationActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LocalisationTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    fun setUp(item: Item) {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), ItemInformationActivity::class.java)
        intent.putExtra(ITEM_INTENT_KEY, item)
        ActivityScenario.launch<ItemInformationActivity>(intent)
    }

    @Test
    fun noLocalisationSet() {
        val myItem = Item("IPhone", ItemType.Phone.ordinal, "IPhone 13", false)
        setUp(myItem)
        Thread.sleep(2000)
        Espresso.onView(ViewMatchers.withId(R.id.item_last_location))
            .check(ViewAssertions.matches(ViewMatchers.withText("Not set")))
    }

    @Test
    fun correctLocalisationDisplayedAfterBadLocationUpdate() {
        val myItem = Item("IPhone", ItemType.Phone.ordinal, "IPhone 13", false)
        myItem.setLastLocation(0.0,0.0) // set bad location
        setUp(myItem)
        Espresso.onView(ViewMatchers.withId(R.id.item_last_location))
            .check(ViewAssertions.matches(ViewMatchers.withText("(0.0, 0.0)")))
    }

    @Test
    fun localisationDoesNotExist(){
        val myItem = Item("IPhone", ItemType.Phone.ordinal, "IPhone 13", false)
        myItem.setLastLocation(1000.0,10000.0) // set bad location (out of map) => exception
        setUp(myItem)
        Thread.sleep(2000)
        Espresso.onView(ViewMatchers.withId(R.id.item_last_location))
            .check(ViewAssertions.matches(ViewMatchers.withText("Not available")))
    }
}
