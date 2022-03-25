package com.github.brugapp.brug

import android.os.Bundle
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.junit.Test

class MyFCMMessagingServiceTest {

    @Test
    fun onNewTokenDoesNothingYetTest() {
        MyFCMMessagingService().onNewToken("")
    }

    @Test
    fun onMessageReceivedDoesNotCrashIfNotificationIsNullTest() {
        MyFCMMessagingService().onMessageReceived(RemoteMessage(Bundle.EMPTY))
    }
}