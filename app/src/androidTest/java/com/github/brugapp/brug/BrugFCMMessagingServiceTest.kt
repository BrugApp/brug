package com.github.brugapp.brug

import com.github.brugapp.brug.messaging.BrugFCMMessagingService
import org.junit.Test

class BrugFCMMessagingServiceTest {

    @Test
    fun onNewTokenDoesNothingYetTest() {
        BrugFCMMessagingService().onNewToken("")
    }
}