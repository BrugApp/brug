package com.github.brugapp.brug

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.ui.SettingsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrugAppTest {
    @get:Rule
    val rule = HiltAndroidRule(this)

    @Test
    fun settingsTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ActivityScenario.launch<SettingsActivity>(
            Intent(context, SettingsActivity::class.java)
        )

        Espresso.onView(ViewMatchers.withId(R.id.titleSettings))
            .check(ViewAssertions.matches(ViewMatchers.withText("Settings")))
    }
}