package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.ui.*
import org.junit.Test

class MapsActivityTest {


    @Test
    fun mapsActivityDisplays(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), MapsActivity::class.java).apply {
            putExtra(EXTRA_LATITUDE, 6.57)
            putExtra(EXTRA_LONGITUDE, 46.52)
            putExtra(EXTRA_BRUG_ITEM_NAME, "EPFL")
        }

        ActivityScenario.launch<MapsActivity>(intent)
    }
}