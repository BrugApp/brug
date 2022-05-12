package com.github.brugapp.brug

import android.os.Bundle
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.junit.Test

class MyFCMMessagingServiceTest {
    private val firebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore = FirebaseFakeHelper().providesFirestore()

    @Test
    fun onNewTokenDoesNothingYetTest() {
        MyFCMMessagingService(firebaseAuth, firestore).onNewToken("")
    }

    @Test
    fun onMessageReceivedDoesNotCrashIfNotificationIsNullTest() {
        MyFCMMessagingService(firebaseAuth, firestore).onMessageReceived(RemoteMessage(Bundle.EMPTY))
    }
}