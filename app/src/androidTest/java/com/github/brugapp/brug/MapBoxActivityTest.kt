package com.github.brugapp.brug

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.ui.*
import org.junit.Rule

class LocationPermissionHelperTest {
    @get:Rule
    var mainActivityRule = ActivityScenarioRule(MapBoxActivity::class.java)

    // We need to test permission granting but Location is granted by default making it untestable.
}