package com.github.brugapp.brug

import android.os.Bundle
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MyFCMMessagingServiceTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun onNewTokenDoesNothingYetTest() {
        MyFCMMessagingService().onNewToken("")
    }

    @Test
    fun onMessageReceivedDoesNotCrashIfNotificationIsNullTest() {
        MyFCMMessagingService().onMessageReceived(RemoteMessage(Bundle.EMPTY))
    }
}