package com.github.brugapp.brug

import com.github.brugapp.brug.messaging.MyFCMMessagingService
import org.junit.Test

class MyFCMMessagingServiceTest {

    @Test
    fun onNewTokenDoesNothingYetTest() {
        MyFCMMessagingService().onNewToken("")
    }
}