package com.github.brugapp.brug

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.messaging.BrugFCMMessagingService
import com.github.brugapp.brug.ui.SignInActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NotificationReceiptTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(SignInActivity::class.java)

    @get:Rule
    val rule = HiltAndroidRule(this)

    @Test
    fun notificationReceiptDoesNotMakeAppCrash() {
        BrugFCMMessagingService.sendNotification(
            ApplicationProvider.getApplicationContext(), "Item found",
            "One of your Items was found !"
        )
        // Wait for notification to disappear
        Thread.sleep(6000)
    }
}