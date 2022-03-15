package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemInformationActivityTest{
    @Test
    fun correctTextDisplayed(){
        val itemsViewModel = (ItemsViewModel(R.drawable.ic_baseline_smartphone_24, "Phone", "Samsung Galaxy S22"))
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemInformationActivity::class.java
        ).apply {
            putExtra("title",itemsViewModel.title)
            putExtra("description",itemsViewModel.description)
            putExtra("image",itemsViewModel.image)
            putExtra("lastLocation",itemsViewModel.lastLocation)
            putExtra("addedOn",itemsViewModel.addedOn)
            putExtra("image",itemsViewModel.image)
        }

        ActivityScenario.launch<ItemInformationActivity>(intent).use {
            onView(ViewMatchers.withId(R.id.item_name)).check(matches(withText("Phone")))
            onView(ViewMatchers.withId(R.id.item_description)).check(matches(withText("Samsung Galaxy S22")))
            onView(ViewMatchers.withId(R.id.tv_name)).check(matches(withText("Phone")))
            onView(ViewMatchers.withId(R.id.item_last_location)).check(matches(withText("Lausanne: Prilly")))
            onView(ViewMatchers.withId(R.id.item_owner)).check(matches(withText("TODO")))
            onView(ViewMatchers.withId(R.id.item_date)).check(matches(withText("11.03.2022")))
        }
    }
}