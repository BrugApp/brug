package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.ItemInformationActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemInformationActivityTest{

    private val str = "no information yet"
    private val itemsViewModel = (Item("Phone", R.drawable.ic_baseline_smartphone_24,"Samsung Galaxy S22"))
    val intent = Intent(
        ApplicationProvider.getApplicationContext(),
        ItemInformationActivity::class.java
    ).apply {
        putExtra("title",itemsViewModel.getName())
        putExtra("description",itemsViewModel.getDescription())
        putExtra("image",itemsViewModel.getId())
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
            onView(ViewMatchers.withId(R.id.item_date)).check(matches(withText(str)))
        }
    }
}