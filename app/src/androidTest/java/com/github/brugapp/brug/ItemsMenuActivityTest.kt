package com.github.brugapp.brug

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemsMenuActivityTest {
    @Test
    fun dummyItemsTest(){
        Intents.init()

        ActivityScenario.launch(ItemsMenuActivity::class.java).use {

        }

        Intents.release()

    }
}