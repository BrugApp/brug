package com.github.brugapp.brug

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.view.View
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.brugapp.brug.ui.NFCScannerActivity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*


class NFCScannerActivityTest {
    @Test
     fun nfcScannerActivityMockTest() {
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        assertThat(mockActivity.adapter, `is`(IsNull.nullValue()))}
    @Test
    fun writeModeOffTest(){
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.writeModeOff()
        assertThat(mockActivity.writeMode,`is`(true))
    }

    @Test
    fun writeModeOnTest(){
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.writeModeOn()
        assertThat(mockActivity.writeMode,`is`(false))
    }

    @Test
    fun findViewsTest(){
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        assertThat(mockActivity.findViews(), `is`(false))
    }

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