package com.github.brugapp.brug

import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.ui.ReportItemActivity
import org.junit.Rule
import org.junit.Test

class ReportItemTest {

    @get:Rule
    var qrCodeScannerActivityRule = ActivityScenarioRule(ReportItemActivity::class.java)

    @Test
    fun reportButtonDisplaysNotificationWithoutCrashing(){
        onView(withId(R.id.report_item_button))
            .perform(ViewActions.click())
        NotificationManagerCompat.from(ApplicationProvider.getApplicationContext()).cancelAll()
        Thread.sleep(1000)
    }
}