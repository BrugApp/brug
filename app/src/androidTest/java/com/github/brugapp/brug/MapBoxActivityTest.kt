package com.github.brugapp.brug

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class LocationPermissionHelperTest {
    @get:Rule
    var mapBoxActivityRule = ActivityScenarioRule(MapBoxActivity::class.java)

    // We need to test permission granting but Location is granted by default making it untestable.
}