package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.ItemInformationActivity
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemInformationActivityTest{

    private val str = "no information yet"
    private val item = Item("Phone","Samsung Galaxy S22",0)

    val intent = Intent(
        ApplicationProvider.getApplicationContext(),
        ItemInformationActivity::class.java
    ).apply {
        if(!MockDatabase.currentUser.getItemList().contains(item)){
            MockDatabase.currentUser.addItem(item)
        }
        putExtra("index",MockDatabase.currentUser.getItemList().indexOf(item))
    }


    @Test
    fun correctTypeDisplayed(){
        ActivityScenario.launch<ItemInformationActivity>(intent).use {
            onView(ViewMatchers.withId(R.id.item_name)).check(matches(withText("Phone")))
            onView(ViewMatchers.withId(R.id.tv_name)).check(matches(withText("Phone")))
        }
    }
    @Test
    fun correctDescriptionDisplayed(){
        ActivityScenario.launch<ItemInformationActivity>(intent).use {
            onView(ViewMatchers.withId(R.id.item_description)).check(matches(withText("Samsung Galaxy S22")))
        }
    }

    @Test
    fun noLocationAndOwnerAndDateYet(){
        ActivityScenario.launch<ItemInformationActivity>(intent).use {
            onView(ViewMatchers.withId(R.id.item_last_location)).check(matches(withText(str)))
            onView(ViewMatchers.withId(R.id.item_owner)).check(matches(withText(str)))
        }
    }

    @Test
    fun declaredItemAsLost(){
        ActivityScenario.launch<ItemInformationActivity>(intent).use {
            assertThat(
                item.isLost(),
                CoreMatchers.`is`(false)
            )
           onView(ViewMatchers.withId(R.id.isLostSwitch)).perform(click())
            assertThat(
                item.isLost(),
                CoreMatchers.`is`(true)
            )
        }
    }



}