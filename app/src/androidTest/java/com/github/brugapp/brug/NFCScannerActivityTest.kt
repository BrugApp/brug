package com.github.brugapp.brug

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import com.github.brugapp.brug.ui.NFCScannerActivity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito
import org.mockito.Mockito.isNotNull


class NFCScannerActivityTest {
    @Test
     fun nfcScannerActivityMockTest() {
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        assertThat(mockActivity.adapter, `is`(IsNull.nullValue()))}

    /*
    @Test
    fun nonNullNfcAdapterTest() {
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val adapter = NfcAdapter.getDefaultAdapter(mockActivity)
        mockActivity.adapter = adapter
        assertThat(mockActivity.adapter, isNotNull())
    }
     */
}