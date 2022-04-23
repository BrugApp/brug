package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers
import com.github.brugapp.brug.ui.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class MapsActivityTest {

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }


    @Test
    fun mapsActivityHasCorrectExtras(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java).apply {
            putExtra(EXTRA_LATITUDE, 6.57)
            putExtra(EXTRA_LONGITUDE, 46.52)
            putExtra(EXTRA_BRUG_ITEM_NAME, "EPFL")
        }

        ActivityScenario.launch<MapsActivity>(intent)
        TODO("test marker")
    }

    @Test
    fun navigateButtonOpensGoogleMaps(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java)
        ActivityScenario.launch<MapsActivity>(intent).use {
            val navigateButton = Espresso.onView(ViewMatchers.withId(R.id.navigateButton))
            navigateButton.perform(ViewActions.click())

            Intents.intended(toPackage("com.google.android.apps.maps"))
        }
    }

}