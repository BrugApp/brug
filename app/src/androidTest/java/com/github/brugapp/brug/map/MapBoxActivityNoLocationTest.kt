package com.github.brugapp.brug.map

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MapBoxActivityNoLocationTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java)
        ActivityScenario.launch<ItemsMenuActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun mapboxDoesNotCrashWhenNoLocationIsProvided() {
        Intents.intended(
            Matchers.allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(MapBoxActivity::class.java.name)
            )
        )
    }



}